# SmartCamera 📸🧠

<p align="center">
  <img src="path_to_your_gif.gif" alt="SmartCamera Demo" width="300">
</p>

## 프로젝트 소개 🚀

SmartCamera는 컴퓨터 비전 기술과 AI를 활용하여 실시간으로 사진 구도와 인물 포즈를 가이드해주는 카메라 애플리케이션입니다. 사용자의 사진 촬영 경험을 한 단계 높여주는 스마트한 기능들을 제공합니다.

## 주요 기능 ✨

- 자동 구도 조정 및 추천
- 객체 추적 및 최적 촬영 시점 제안
- AI 기반 인물 비율 계산
- 머리 위 여백 최적화

## 기술 스택 🛠️

- **프레임워크**: Android (Kotlin)
- **UI**: Jetpack Compose
- **카메라 API**: CameraX, Camera2
- **AI/ML**:
  - MediaPipe
  - TensorFlow Lite
  - ML Kit
- **그래픽**: Android Canvas
- **아키텍처**: MVVM

## 핵심 구현 사항 💻

1. **실시간 AI 처리**
   - MediaPipe를 활용한 인체 주요 부위 감지
   - ML Kit을 이용한 object tracking

2. **동적 UI 오버레이**
   - Android Canvas를 사용한 실시간 그래픽 오버레이
   - 인물 윤곽선, 가이드 박스 등 동적 요소 구현

3. **지능형 촬영 가이드**
   - 인물 비율, 머리 위 공백, 전체 구도 등 실시간 분석
   - 분석 결과에 기반한 최적화 가이드 메시지 제공

4. **고성능 카메라 통합**
   - CameraX와 Camera2 API를 활용한 고급 카메라 기능 구현
     * 객체 클릭 시 자동 추적(tracking) 및 초점 조정
     * 객체 추적 중 지속적인 초점 재조정으로 선명한 이미지 유지
     * 화이트 밸런스, ISO, 셔터 스피드 등 전문 촬영 파라미터 수동 조정 기능
     * 직관적인 제스처를 통한 확대/축소 및 밝기 조절
   

5. **모던 UI/UX**
   - Jetpack Compose를 활용한 선언적 UI 구현
   - 부드러운 애니메이션과 직관적인 사용자 인터페이스 설계

## 프로젝트 성과 및 학습 🏆

- 실시간 데이터 처리와 UI 업데이트의 효율적인 동기화 방법 학습
- 다양한 AI/ML 라이브러리의 통합 및 최적화 기술 향상

## 분석 결과 예시 📊

아래는 SmartCamera의 AI 분석 결과를 보여주는 예시입니다:

<table>
  <tr>
    <td align="center">
      <img src="path_to_analysis_image_1.jpg" alt="분석 결과 1" width="400"><br>
      <em>인물 구도 최적화 및 포즈 가이드</em>
    </td>
    <td align="center">
      <img src="path_to_analysis_image_2.jpg" alt="분석 결과 2" width="400"><br>
      <em>객체 추적 및 초점 조정 예시</em>
    </td>
  </tr>
</table>

