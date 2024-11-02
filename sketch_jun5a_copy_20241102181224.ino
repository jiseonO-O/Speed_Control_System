#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <Wire.h>
#include <Adafruit_MotorShield.h>

// Firebase 설정
#define FIREBASE_HOST "https://smartspeedcontrolsystem-default-rtdb.firebaseio.com/" // Firebase Database URL
#define FIREBASE_AUTH "bRAeNmslv13Pu3vdcxl2FdTxxRTe2hktneZY36K8" // Firebase Database Secret Key

// WiFi 설정
const char* WIFI_SSID = "김지선의 iPhone";
const char* WIFI_PASSWORD = "52865230";

// 모터 쉴드 초기화
Adafruit_MotorShield AFMS = Adafruit_MotorShield();

// 모터 선언
Adafruit_DCMotor *motor1 = AFMS.getMotor(1);
Adafruit_DCMotor *motor2 = AFMS.getMotor(2);
Adafruit_DCMotor *motor3 = AFMS.getMotor(3);
Adafruit_DCMotor *motor4 = AFMS.getMotor(4);

// Firebase 데이터 경로
String andval_path = "/ANDVAL";
String yoloval_path = "/YOLOVAL";

// 모터 속도 설정
int default_speed = 255; // 기본 속도 설정
int current_speed = default_speed; // 초기값은 기본 속도로 설정
String last_yoloval = ""; // 마지막으로 받은 YOLOVAL 값을 저장

FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

void setup() {
  Serial.begin(115200);
  
  // WiFi 연결
  Serial.println("Connecting to Wi-Fi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("Connected to Wi-Fi with IP: " + WiFi.localIP().toString());
  
  // Firebase 초기화
  Serial.println("Initializing Firebase...");
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  firebaseData.setResponseSize(1024); // Response 사이즈 설정
  
  if (Firebase.ready()) {
    Serial.println("Firebase initialized successfully");
  } else {
    Serial.println("Failed to initialize Firebase");
  }

  // 모터 쉴드 초기화
  AFMS.begin();
  motor1->setSpeed(default_speed);
  motor1->run(FORWARD);
  motor2->setSpeed(default_speed);
  motor2->run(FORWARD);
  motor3->setSpeed(default_speed);
  motor3->run(FORWARD);
  motor4->setSpeed(default_speed);
  motor4->run(FORWARD);

  // Firebase 스트리밍 시작
  if (!Firebase.beginStream(firebaseData, yoloval_path)) {
    Serial.println("Could not begin stream");
    Serial.println(firebaseData.errorReason());
  }
}

void loop() {
  // Firebase 스트림 데이터 확인
  if (!Firebase.readStream(firebaseData)) {
    Serial.println("Stream read failed");
    Serial.println(firebaseData.errorReason());
  }

  if (firebaseData.streamTimeout()) {
    Serial.println("Stream timeout, reconnecting...");
    if (!Firebase.beginStream(firebaseData, yoloval_path)) {
      Serial.println("Could not begin stream");
      Serial.println(firebaseData.errorReason());
    }
  }

  if (firebaseData.streamAvailable()) {
    if (firebaseData.dataType() == "string") {
      last_yoloval = firebaseData.stringData();
      Serial.print("Received value for YOLOVAL: ");
      Serial.println(last_yoloval);
      
      // YOLOVAL 값에 따라 처리
      if (last_yoloval == "1ST") {
        // ANDVAL 값을 읽어와서 처리
        Serial.print("Getting ANDVAL...");
        if (Firebase.getString(firebaseData, andval_path)) {
          String andval = firebaseData.stringData();
          Serial.print("Received value for ANDVAL: ");
          Serial.println(andval);
          if (andval == "OK") {
            current_speed = default_speed * 0.9; // 받은 값이 "OK"이면 기본 속도의 90%로 설정
          } else {
            current_speed = default_speed; // 그 외의 경우 기본 속도로 설정
          }
        } else {
          Serial.print("Failed to get value for ANDVAL: ");
          Serial.println(firebaseData.errorReason());
          current_speed = default_speed; // ANDVAL 값을 가져오는데 실패한 경우 기본 속도로 설정
        }
      } else if (last_yoloval == "2ST") {
        current_speed = default_speed * 0.5; // 받은 값이 "2ST"이면 기본 속도의 50%로 설정
      } else {
        // ANDVAL 값을 읽어와서 처리
        Serial.print("Getting ANDVAL...");
        if (Firebase.getString(firebaseData, andval_path)) {
          String andval = firebaseData.stringData();
          Serial.print("Received value for ANDVAL: ");
          Serial.println(andval);
          if (andval == "OK") {
            current_speed = default_speed * 0.7; // 받은 값이 "OK"이면 기본 속도의 70%로 설정
          } else {
            current_speed = default_speed; // 그 외의 경우 기본 속도로 설정
          }
        } else {
          Serial.print("Failed to get value for ANDVAL: ");
          Serial.println(firebaseData.errorReason());
          current_speed = default_speed; // ANDVAL 값을 가져오는데 실패한 경우 기본 속도로 설정
        }
      }
    }
  }

  // 모터 속도 설정
  motor1->setSpeed(current_speed);
  motor1->run(FORWARD);
  motor2->setSpeed(current_speed);
  motor2->run(FORWARD);
  motor3->setSpeed(current_speed);
  motor3->run(FORWARD);
  motor4->setSpeed(current_speed);
  motor4->run(FORWARD);

  delay(500); // 0.5초마다 업데이트
}
