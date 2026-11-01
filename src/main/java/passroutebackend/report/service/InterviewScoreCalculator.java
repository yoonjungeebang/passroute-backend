package passroutebackend.interview.service;

import org.springframework.stereotype.Component;
import passroutebackend.interview.entity.FaceAnalysis;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.VoiceAnalysis;

import java.time.Duration;
import java.util.List;

@Component
public class InterviewScoreCalculator {

  public double calcVoiceScore(List<VoiceAnalysis> voiceList, double totalMinutes) {
    if (voiceList.isEmpty()) return 0.0;
    boolean hasData = voiceList.stream().anyMatch(v ->
        v.getAvgWpm() != null || v.getFillerCount() != null || v.getAvgSilenceDuration() != null);
    if (!hasData) return 0.0;

    double avgWpm = voiceList.stream()
        .filter(v -> v.getAvgWpm() != null)
        .mapToDouble(v -> v.getAvgWpm())
        .average().orElse(0.0);

    long totalFiller = voiceList.stream()
        .filter(v -> v.getFillerCount() != null)
        .mapToLong(v -> v.getFillerCount())
        .sum();
    double fillerPerMin = totalMinutes > 0 ? totalFiller / totalMinutes : 0.0;

    double avgSilence = voiceList.stream()
        .filter(v -> v.getAvgSilenceDuration() != null)
        .mapToDouble(v -> v.getAvgSilenceDuration())
        .average().orElse(0.0);

    double wpmScore = calcWpmScore(avgWpm);
    double fillerScore = clamp((10 - fillerPerMin) / 10.0 * 100);
    double silenceScore = clamp((15 - avgSilence) / 10.0 * 100);

    return wpmScore * 0.30 + fillerScore * 0.35 + silenceScore * 0.35;
  }

  public double calcFaceScore(List<FaceAnalysis> faceList, double totalMinutes) {
    if (faceList.isEmpty()) return 0.0;
    boolean hasData = faceList.stream().anyMatch(f ->
        f.getAvgGazeRatio() != null || f.getGazeOffCount() != null || f.getAvgBlinkPerMin() != null);
    if (!hasData) return 0.0;

    double avgGazeRatio = faceList.stream()
        .filter(f -> f.getAvgGazeRatio() != null)
        .mapToDouble(f -> f.getAvgGazeRatio())
        .average().orElse(0.0);

    long totalGazeOff = faceList.stream()
        .filter(f -> f.getGazeOffCount() != null)
        .mapToLong(f -> f.getGazeOffCount())
        .sum();
    double gazeOffPerMin = totalMinutes > 0 ? totalGazeOff / totalMinutes : 0.0;

    double avgBlink = faceList.stream()
        .filter(f -> f.getAvgBlinkPerMin() != null)
        .mapToDouble(f -> f.getAvgBlinkPerMin())
        .average().orElse(0.0);

    double gazeRatioScore = clamp(avgGazeRatio);
    double gazeOffScore = clamp((10 - gazeOffPerMin) / 10.0 * 100);
    double blinkScore = avgBlink <= 25 ? 100.0 : clamp((50 - avgBlink) / 25.0 * 100);

    return gazeRatioScore * 0.30 + gazeOffScore * 0.35 + blinkScore * 0.35;
  }

  public double calcTotalMinutes(InterviewSession session) {
    if (session.getStartedAt() == null || session.getEndedAt() == null) return 1.0;
    long seconds = Duration.between(session.getStartedAt(), session.getEndedAt()).toSeconds();
    return Math.max(seconds / 60.0, 0.01);
  }

  private double calcWpmScore(double wpm) {
    if (wpm >= 100 && wpm <= 180) return 100.0;
    if (wpm < 100) return clamp((wpm - 50) / 50.0 * 100);
    return clamp((250 - wpm) / 70.0 * 100);
  }

  private double clamp(double value) {
    return Math.max(0.0, Math.min(100.0, value));
  }
}
