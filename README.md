## SPEED_CONTROL_SYSTEM


####  이 프로젝트는 어린이 보호구역에서 발생할 수 있는 교통사고를 예방하기 위해 설계되었습니다.
####  사용자 위치와 표지판 인식을 통해 차량 속도를 실시간으로 제어하며 어린이 보호구역 내에서는 지정된 속도 제한을 강제합니다.
####  RC카를 통해 시스템을 시뮬레이션하며 Firebase를 사용한 데이터 통신과 YOLOv5 기반 객체 탐지를 통해 정확한 표지판 인식을 구현합니다.
####  이 시스템은 스마트한 교통 제어를 통해 어린이 안전을 강화하고 사고를 미연에 방지하는 데 목적을 둡니다. 

****


### 기능 설명

이 프로젝트는 어린이 보호구역 내 사고 예방을 위해 개발된 시스템입니다.

사용자가 보호구역 반경 200m 내에 접근하면 앱이 이를 감지해 차량 속도를 50km/h로 제한하고

라즈베리파이 카메라를 이용해 30km 속도 제한 표지판을 인식합니다.

보호구역 접근과 표지판 인식이 모두 확인되면 차량 속도를 30km/h로 조정합니다.

RC카로 차량을 모사하며 Firebase를 통해 실시간 통신 및 제어가 이루어집니다.


***


### 주요기능
+ 구글지도 API통합 :
 구글 지도 API를 사용하여 어린이 보호구역을 식별하고, 이 정보를 시스템에 통합합니다.


+ FireBase 데이터베이스 설정 :
   FireBase를 사용하여 자동차의 위치 데이터를 저장하고 관리합니다.

  
+ 아두이노를 활용한 시스템 구현 :
   FireBase에서 수신한 RC Car의 위치 정보를 기반으로 어린이 보호구역 내에 진입할 경우 아두이노를 사용하여 차량의 속도를 제어하는 시스템을 구현합니다.

  
+ Object Detection :
    Object Detection을 사용하여 어린이 보호구역 내의 교통 표지판을 인식합니다.

****

### 기술 스택
+ Android Studio

+ Object Detection

+ Raspberry Pi

+ Firebase

+  Arduino

+ YOLOv5 커스텀 학습 모델 활용
