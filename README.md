### 프로젝트 소개
<hr>

유튜브 플레이리스트 영상 분할 서비스.

사용자가 플레이리스트 링크를 제출하면 해당 영상 페이지에 있는 타임스탬프 정보를 스크래핑하여 각 노래를 검색하고 재생목록에도 추가할 수 있게 도와준다.

~https://doplaylist.com~ // 현재 AWS EC2 프리티어 사용량 이슈로 접속불가

프로젝트에서 플레이리스트는 여러 노래를 이어붙여 장시간으로 만든 하나의 플레이리스트 영상을 말함.
프로젝트에서 재생목록은 사용자가 생성한 유튜브 재생목록을 말함.

### 기간 / 인원
<hr>

(2024.3~2024.5) 1인 프로젝트

### 기능
<hr>

1. Google OAuth2 로그인으로 해당 아이디 유튜브 재생목록에 접근
2. 플레이리스트 링크를 입력받으면 Selenium으로 해당 페이지 스크래핑
3. 스크래핑한 데이터에서 곡을 특정할 수 있는 데이터(타임스탬프, youtube music정보란)에서 가수와 곡 제목 추출(playlist service니까 해당 클래스의 코드에 링크?)
4. 추출한 각 곡에 대해 사용자의 재생목록에 추가하거나 유튜브 검색 링크로 link
5. 한번 검색된 플레이리스트 정보는 DB에 저장되고 이후 빠른 반환이 가능함
![DoPlayList sequence diagram](https://github.com/user-attachments/assets/ca752a98-db82-4948-8ff0-798a8f9afe79)
![DoPlayList ERD](https://github.com/user-attachments/assets/494f9aee-6518-4a01-8e0b-173d705550e6)


### 기술
<hr>

+ 언어 : JAVA, javascript, css, html
+ 프레임워크 : SpringBoot, Spring JPA
+ DB : mariadb(AWS EC2 배포 서버와 같이 사용)
+ 템플릿 엔진 : thymeleaf
+ 라이브러리 : spring security, Caffeine cache, Selenium Java, Jwt, Http WebClient, bonigarcia의 WebDriverManager(https://github.com/bonigarcia/webdrivermanager), lombok
+ API : Google OAuth2, youtube data api 
+ 서버 및 배포: AWS EC2 Linux(우분투 버전)에 jar 배포, AWS Route53

### 학습 사항
<hr>

+ 웹호스팅(도메인 구매, https 인증서 발급, 도메인 연결)
+ Google Cloud Platform에서 테스팅 단계에서 실제 서비스 단계로의 인증 절차
+ Selenium Java의 사용, ChromeDriver 업데이트 이슈를 해결하기 위한 bonigarcia의 WebDriverManager 라이브러리 적용
+ Youtube data api v3 사용법. 해당 api의 개발 가이드및 예시 코드를 적극 참조


### 트러블 / 피드백
<hr>

+ #### Youtube data api v3 사용의 할당량 문제
	+ 할당량은 1일단 10000을 제공 받는데 이는 약 200곡을 재생목록에 추가할 수 있다.
	+ 한번 이용에 5곡을 추가한다고 가정했을때 40명이 하루 사이트 사용 최대
	+ 해결 방법 : 
		+ 할당량 추가 신청(서류 작업이 복잡하고 오래걸림)
		+ Google Cloud Platform의 프로젝트 단위로 할당량을 받기 때문에 여러 프로젝트를 만들어 할당량을 쓸 때마다 프로젝트의 client_secret을 바꾸면 해결. 본 프로젝트는 구글 로그인을 겸하기 때문에 적용하기에 부적합. 구글 로그인을 쓰지 않고 유튜브 api만 사용하는 경우에는 유효할 듯.
		+ 재생목록에 한곡의 추가라는 시스템이 바뀌지 않으면 할당량 최적화가 어려움. 할당량 부족 시 사용자가 직접 추가할 수 있게 링크를 대신 제공하는 등의 편의 기능이 필요함. 
+ #### Google Cloud Platform 서비스 전환 및 구글 페이지 색인 문제
	+ 프로젝트에서 사용자 유튜브 재생목록에 접근하는 민감한 작업을 하기 때문에 개인 정보 처리 약관, 개인 정보 보호 정책 등 추가할 내용이 많았음. 
	+ google에서 요구하는 사용자의 개인정보 방침에 대한 공지에 대한 페이지가 구글 페이지 색인이 되지 않아 이를 해결하는데 실제 서비스 단계로의 전환에 오랜 기간이 걸림. 
+ #### Selenium 사용의 동시 요청 고려를 못함
	+ 셀레니움은 일반적으로 싱글 스레드를 사용하므로 한번에 한명의 작업만 할 수 있다.
	+ 해결 방법 : 사용자의 요청을 별도의 스레드에서 처리하도록 설정해 각 스레드에서 별도로 셀레니움 인스턴스를 생성하게 하면 됨
		-> 해결 완료. AsyncConfig Class 파일을 생성해 Executor를 반환하는 컨피큐어 파일 설정.
+ #### Selenium 사용의 코드 유지 보수가 필요함
	+ 유튜브 페이지의 html 구조가 바뀐다면 코드가 그에 맞게 수정되어야 함.
	+ 수정을 최대한 피하기 위해서는 xpath값을 이용한 element 생성을 지양하고 class명과 id값을 이용하는 것이 좋다. 
+ #### 정확도 문제
	+ 플레이리스트의 노래 정보를 주로 게시된 타임라인 문자열 데이터에 의존하므로 이를 후처리하는 과정에서 정확도 문제가 생길 수 있다. 
	+ Case 1 ) 첫 번째 댓글, 더보기에서 타임스탬프의 유무를 파악할때 html의 a 태그의 유무를 살피고 그 다음 a 태그안의 내용이 타임스탬프 패턴과 동일 할때 이를 플레이리스트의 노래 타임스탬프라고 파악하는데 노래의 타임스탬프가 아니면서 동영상의 타임스탬프가 포함되어있으면 오류.
	+ Case 2 ) 가장 일반적인 노래 타임스탬프인 '타임스탬프 가수 - 노래제목'을 베이스로 가정하였기에 그외의 다른 특수문자가 설명이 추가되어있을 시 부정확함.
