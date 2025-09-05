import os
import subprocess
from github import Github
import google.generativeai as genai
import re
import json

# GitHub 토큰 설정
GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
if not GITHUB_TOKEN:
    raise ValueError("GITHUB_TOKEN 환경 변수가 설정되지 않았습니다.")

# Gemini API 키 설정
GEMINI_API_KEY = os.environ.get('GEMINI_API_KEY')
if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY 환경 변수가 설정되지 않았습니다. GitHub Secrets에 추가해주세요.")

# Gemini 모델 설정
genai.configure(api_key=GEMINI_API_KEY)
model = genai.GenerativeModel('gemini-2.0-flash')

# GitHub 리포지토리 정보
REPOSITORY_SLUG = os.environ.get('GITHUB_REPOSITORY')
if not REPOSITORY_SLUG:
    raise ValueError("GITHUB_REPOSITORY 환경 변수가 설정되지 않았습니다.")

g = Github(GITHUB_TOKEN)
repo = g.get_repo(REPOSITORY_SLUG)

# 현재 푸시된 브랜치 가져오기
github_ref = os.environ.get('GITHUB_REF', '')
if github_ref.startswith('refs/heads/'):
    HEAD_BRANCH = github_ref.replace('refs/heads/', '')
else:
    # fallback: 현재 브랜치를 git으로 확인
    try:
        HEAD_BRANCH = subprocess.check_output(['git', 'rev-parse', '--abbrev-ref', 'HEAD'], text=True).strip()
    except:
        HEAD_BRANCH = 'unknown'

# BASE_BRANCH 동적으로 설정 (가장 최근 열린 PR의 head 브랜치)
BASE_BRANCH = 'main' # 기본값은 main
try:
    # 열려있는 PR을 최신순으로 정렬하여 하나 가져옵니다.
    # state='open', sort='created', direction='desc'
    open_prs = repo.get_pulls(state='open', sort='created', direction='desc')
    latest_open_pr = next(iter(open_prs), None) # 첫 번째 PR을 가져오거나 없으면 None

    if latest_open_pr and latest_open_pr.head.ref != HEAD_BRANCH:
        # 가장 최근 PR의 head 브랜치를 BASE_BRANCH로 설정
        BASE_BRANCH = latest_open_pr.head.ref
        print(f"✅ 최근 열린 PR ({latest_open_pr.title})의 head 브랜치 '{BASE_BRANCH}'를 BASE_BRANCH로 설정합니다.")
    elif latest_open_pr and latest_open_pr.head.ref == HEAD_BRANCH:
        BASE_BRANCH = latest_open_pr.head.ref
        print(f"⚠️ 최근 열린 PR의 head 브랜치('{latest_open_pr.head.ref}')가 현재 브랜치('{HEAD_BRANCH}')와 동일합니다.")
    else:
        print(f"ℹ️ 열려있는 PR이 없습니다. 'main'을 BASE_BRANCH로 유지합니다.")

except Exception as e:
    print(f"⚠️ 최근 PR 정보를 가져오는 중 오류 발생: {e}. 'main'을 BASE_BRANCH로 유지합니다.")
    BASE_BRANCH = 'main' # 오류 발생 시 기본값으로 main 설정


print(f"HEAD_BRANCH: {HEAD_BRANCH}")
print(f"BASE_BRANCH: {BASE_BRANCH}")

# 동일 브랜치 체크
if HEAD_BRANCH == BASE_BRANCH:
    print(f"HEAD_BRANCH와 BASE_BRANCH가 동일합니다. PR을 생성하지 않습니다.")
    exit(0)

# 기존 PR 체크 (HEAD_BRANCH -> BASE_BRANCH)
existing_prs = repo.get_pulls(state='open', head=f"{REPOSITORY_SLUG.split('/')[0]}:{HEAD_BRANCH}", base=BASE_BRANCH)
if existing_prs.totalCount > 0:
    print(f"기존 PR이 존재합니다: {existing_prs[0].html_url}")
    with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
        f.write(f"pr_url={existing_prs[0].html_url}\n")
    exit(0)

def get_commit_history_and_files():
    """Git 로그와 변경된 파일 목록을 가져옵니다."""
    try:
        subprocess.run(['git', 'fetch', 'origin', BASE_BRANCH], check=True, capture_output=True)

        # 모든 커밋 메시지 가져오기 (BASE_BRANCH 대비 HEAD_BRANCH의 추가된 커밋)
        commit_messages_raw = subprocess.check_output(
            ['git', 'log', f'origin/{BASE_BRANCH}..HEAD', '--pretty=format:%s'],
            text=True, encoding='utf-8'
        ).strip()

        if not commit_messages_raw:
            # 커밋이 없다면 최근 1개 커밋이라도 가져오기 (이 부분은 BASE_BRANCH에 대한 상대적인 커밋이 없는 경우를 대비한 것이므로,
            # BASE_BRANCH가 잘 설정되었다면 이 블록은 거의 실행되지 않을 것입니다.)
            print(f"⚠️ {BASE_BRANCH} 대비 HEAD에 새로운 커밋이 없습니다. 최근 1개 커밋을 가져옵니다.")
            commit_messages_raw = subprocess.check_output(
                ['git', 'log', '--pretty=format:%s', '-1'],
                text=True, encoding='utf-8'
            ).strip()

        commit_messages = [msg for msg in commit_messages_raw.split('\n') if msg.strip()]

        # 변경된 파일 (BASE_BRANCH...HEAD)
        try:
            changed_files_raw = subprocess.check_output(
                ['git', 'diff', '--name-only', f'origin/{BASE_BRANCH}...HEAD'],
                text=True, encoding='utf-8'
            ).strip()
            changed_files = [f for f in changed_files_raw.split('\n') if f.strip()] if changed_files_raw else []
        except subprocess.CalledProcessError:
            # diff 명령어가 실패하는 경우 (예: 브랜치 히스토리가 완전히 다를 때) show 명령어로 시도
            print(f"⚠️ git diff 실패. git show로 변경된 파일 확인 시도.")
            try:
                changed_files_raw = subprocess.check_output(
                    ['git', 'show', '--name-only', '--format=', 'HEAD'], # HEAD의 변경사항만
                    text=True, encoding='utf-8'
                ).strip()
                changed_files = [f for f in changed_files_raw.split('\n') if f.strip()][:20] if changed_files_raw else []
            except:
                print("⚠️ git show도 실패했습니다. 변경된 파일 목록을 가져올 수 없습니다.")
                changed_files = []

        print(f"📝 커밋 메시지 {len(commit_messages)}개, 변경된 파일 {len(changed_files)}개 발견 ({BASE_BRANCH} 기준)")
        return commit_messages, changed_files

    except Exception as e:
        print(f"Git 명령어 실행 중 오류 발생: {e}")
        return [], []


def generate_pr_content_with_gemini(commit_messages, changed_files):
    """Gemini를 사용하여 PR 제목과 본문을 생성합니다."""

    commit_list = '\n'.join(commit_messages) if commit_messages else "커밋 메시지 정보를 가져올 수 없습니다."
    files_list = '\n'.join(changed_files) if changed_files else "파일 정보를 가져올 수 없습니다."

    unified_prompt = f"""GitHub Pull Request의 제목과 본문을 작성해주세요.
이 프로젝트는 **에어비앤비를 모티브로 한 Spring Boot 숙박 예약 API 서버 프로젝트**입니다.
사용자는 **게스트로 숙소를 예약하거나, 호스트로 숙소를 등록하고 게스트의 예약을 승인**할 수 있습니다.

커밋 메시지 ({len(commit_messages)}개):
{commit_list}

변경된 파일 ({len(changed_files)}개):
{files_list}

다음 JSON 형식으로 응답해주세요:
{{
  "title": "간결하고 명확한 PR 제목 (커밋 메시지들을 종합하여 작성)",
  "body": "마크다운 형식의 본문 (변경사항 요약, 변경 동기 포함)"
}}

본문 작성 가이드라인:
- 변경사항을 카테고리별로 정리 (기능 추가, 버그 수정, 리팩토링 등)
- Spring Boot 관련 변경사항, 데이터베이스 스키마 변경, API 엔드포인트 변경, 보안 관련 변경 등이 있다면 명시
- **호스트 또는 게스트 기능과 관련된 변경이라면 구체적으로 언급**
- 변경된 파일명 나열 금지
- 기술 부채 해소, 성능 개선 등 변경 동기를 명확히 설명"""

    try:
        response = model.generate_content(unified_prompt)
        response_text = response.text.strip()

        # JSON 파싱 시도
        try:
            # JSON 코드 블록에서 JSON 부분만 추출
            json_match = re.search(r'```json\s*(\{.*?\})\s*```', response_text, re.DOTALL)
            if json_match:
                json_str = json_match.group(1)
            else:
                # JSON 코드 블록이 없으면 전체 텍스트에서 JSON 찾기
                json_match = re.search(r'\{.*\}', response_text, re.DOTALL)
                json_str = json_match.group(0) if json_match else response_text

            parsed_response = json.loads(json_str)
            pr_title = parsed_response.get('title', '').strip()
            pr_body = parsed_response.get('body', '').strip()

        except (json.JSONDecodeError, AttributeError):
            # JSON 파싱 실패 시 기본값
            pr_title = f"{HEAD_BRANCH} 브랜치 변경사항"
            pr_body = generate_default_body(commit_messages, changed_files)

        # 제목 정리
        pr_title = pr_title.replace('"', '').replace("'", '').replace('**', '').replace('#', '').strip()
        if len(pr_title) > 100:
            pr_title = pr_title[:97] + "..."

        # 본문이 비어있으면 기본 본문 생성
        if not pr_body:
            pr_body = generate_default_body(commit_messages, changed_files)

        # AI 생성 문구 추가
        pr_body += f"\n\n---\n*이 PR은 Gemini AI에 의해 자동 생성되었습니다. (커밋 {len(commit_messages)}개, 파일 {len(changed_files)}개 분석, 대상 브랜치: `{BASE_BRANCH}`)*"

        return pr_title, pr_body

    except Exception as e:
        print(f"Gemini API 호출 중 오류 발생: {e}")
        return f"{HEAD_BRANCH} 브랜치 변경사항", generate_default_body(commit_messages, changed_files)

def generate_default_body(commit_messages, changed_files):
    """기본 PR 본문 생성"""
    body = f"""## 변경 사항 요약

이 PR은 `{HEAD_BRANCH}` 브랜치의 변경사항을 `{BASE_BRANCH}` 브랜치에 병합합니다.

### 커밋 메시지 ({len(commit_messages)}개)
{chr(10).join(f"- {msg}" for msg in commit_messages)}

### 변경된 파일 ({len(changed_files)}개)
{chr(10).join(f"- `{file}`" for file in changed_files[:10])}"""

    if len(changed_files) > 10:
        body += f"\n- ... 외 {len(changed_files) - 10}개 파일"

    return body

# 메인 로직
if __name__ == "__main__":
    commit_msgs, changed_files = get_commit_history_and_files()

    if not (commit_msgs or changed_files):
        print("⚠️ 커밋이나 변경된 파일이 없어 PR을 생성할 수 없습니다.")
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write("pr_url=no_changes\n")
        exit(0)

    # PR 제목과 본문 생성
    pr_title, pr_body = generate_pr_content_with_gemini(commit_msgs, changed_files)

    # PR 생성
    try:
        pull_request = repo.create_pull(
            title=pr_title,
            body=pr_body,
            head=HEAD_BRANCH,
            base=BASE_BRANCH,
            draft=True
        )
        print(f"✅ PR 초안이 생성되었습니다: {pull_request.html_url}")

        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f"pr_url={pull_request.html_url}\n")
            f.write(f"pr_number={pull_request.number}\n")

    except Exception as e:
        print(f"⚠️ PR 생성 실패: {e}")
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write("pr_url=creation_failed\n")