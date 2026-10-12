export class VoiceAudioEngine {
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private audioStream: MediaStream | null = null;
  private audioContext: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private currentAudioElement: HTMLAudioElement | null = null;
  private currentPlayingId: string | null = null;
  private animFrameId: number | null = null;

  // Recording status
  private isRecording = false;
  private isPaused = false;

  async startRecording(
    onAmplitude: (amplitude: number) => void
  ): Promise<boolean> {
    this.stopPlayback();
    this.audioChunks = [];
    this.isRecording = true;
    this.isPaused = false;

    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        this.audioStream = stream;

        const AudioContextClass =
          window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
        this.audioContext = new AudioContextClass();
        const source = this.audioContext.createMediaStreamSource(stream);
        this.analyser = this.audioContext.createAnalyser();
        this.analyser.fftSize = 64;
        source.connect(this.analyser);

        const dataArray = new Uint8Array(this.analyser.frequencyBinCount);
        const pollAudio = () => {
          if (!this.isRecording) return;
          if (!this.isPaused && this.analyser) {
            this.analyser.getByteFrequencyData(dataArray);
            let sum = 0;
            for (let i = 0; i < dataArray.length; i++) {
              sum += dataArray[i];
            }
            const avg = sum / dataArray.length;
            const norm = Math.max(0.1, Math.min(1.0, avg / 128));
            onAmplitude(norm);
          } else {
            onAmplitude(0.12);
          }
          this.animFrameId = requestAnimationFrame(pollAudio);
        };
        this.animFrameId = requestAnimationFrame(pollAudio);

        const recorder = new MediaRecorder(stream);
        recorder.ondataavailable = (e) => {
          if (e.data.size > 0) {
            this.audioChunks.push(e.data);
          }
        };
        recorder.start(100);
        this.mediaRecorder = recorder;
        return true;
      }
    } catch {
      // If mic permission blocked, fallback to simulated waveform
    }

    // Simulated microphone loop fallback
    const interval = setInterval(() => {
      if (!this.isRecording) {
        clearInterval(interval);
        return;
      }
      if (!this.isPaused) {
        const sim = 0.15 + Math.random() * 0.7;
        onAmplitude(sim);
      } else {
        onAmplitude(0.1);
      }
    }, 80);

    return false;
  }

  pauseRecording() {
    this.isPaused = true;
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      try {
        this.mediaRecorder.pause();
      } catch {}
    }
  }

  resumeRecording() {
    this.isPaused = false;
    if (this.mediaRecorder && this.mediaRecorder.state === 'paused') {
      try {
        this.mediaRecorder.resume();
      } catch {}
    }
  }

  async stopRecording(): Promise<{ url: string; blob: Blob | null }> {
    this.isRecording = false;
    this.isPaused = false;

    if (this.animFrameId) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = null;
    }

    return new Promise((resolve) => {
      if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
        this.mediaRecorder.onstop = () => {
          const blob = new Blob(this.audioChunks, { type: 'audio/webm' });
          const url = URL.createObjectURL(blob);
          this.cleanupStream();
          resolve({ url, blob });
        };
        try {
          this.mediaRecorder.stop();
        } catch {
          this.cleanupStream();
          resolve(this.createFallbackBeepAudio());
        }
      } else {
        this.cleanupStream();
        resolve(this.createFallbackBeepAudio());
      }
    });
  }

  cancelRecording() {
    this.isRecording = false;
    this.isPaused = false;
    if (this.animFrameId) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = null;
    }
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      try {
        this.mediaRecorder.stop();
      } catch {}
    }
    this.cleanupStream();
    this.audioChunks = [];
  }

  private cleanupStream() {
    if (this.audioStream) {
      this.audioStream.getTracks().forEach((t) => t.stop());
      this.audioStream = null;
    }
    if (this.audioContext && this.audioContext.state !== 'closed') {
      this.audioContext.close().catch(() => {});
      this.audioContext = null;
    }
    this.analyser = null;
    this.mediaRecorder = null;
  }

  // Create synthetic sound wave if no physical mic recorded
  private createFallbackBeepAudio(): { url: string; blob: Blob | null } {
    try {
      const AudioCtx =
        window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      const ctx = new AudioCtx();
      const sampleRate = ctx.sampleRate;
      const duration = 2.5;
      const frameCount = sampleRate * duration;
      const audioBuffer = ctx.createBuffer(1, frameCount, sampleRate);
      const channel = audioBuffer.getChannelData(0);

      for (let i = 0; i < frameCount; i++) {
        const t = i / sampleRate;
        // pleasant musical melodic voice tone
        const noteFreq = 440 + Math.sin(t * 8) * 40;
        channel[i] = Math.sin(2 * Math.PI * noteFreq * t) * 0.15 * Math.exp(-t * 0.4);
      }

      // Convert audio buffer to WAV blob
      const wavBlob = audioBufferToWav(audioBuffer);
      const url = URL.createObjectURL(wavBlob);
      ctx.close().catch(() => {});
      return { url, blob: wavBlob };
    } catch {
      return { url: '', blob: null };
    }
  }

  // Playback control
  playAudio(
    id: string,
    src: string,
    onProgress: (progress: number, currentTime: number, duration: number) => void,
    onEnded: () => void
  ) {
    if (this.currentPlayingId === id && this.currentAudioElement) {
      if (this.currentAudioElement.paused) {
        this.currentAudioElement.play().catch(() => {});
      } else {
        this.currentAudioElement.pause();
      }
      return;
    }

    this.stopPlayback();

    const audio = new Audio(src);
    this.currentAudioElement = audio;
    this.currentPlayingId = id;

    audio.ontimeupdate = () => {
      const cur = audio.currentTime;
      const dur = audio.duration || 1;
      const prog = Math.min(1, Math.max(0, cur / dur));
      onProgress(prog, cur, dur);
    };

    audio.onended = () => {
      this.currentPlayingId = null;
      this.currentAudioElement = null;
      onEnded();
    };

    audio.onerror = () => {
      this.currentPlayingId = null;
      this.currentAudioElement = null;
      onEnded();
    };

    audio.play().catch(() => {
      // In case browser autoplay blocks or file format issue
      this.simulatePlayback(id, 3, onProgress, onEnded);
    });
  }

  simulatePlayback(
    id: string,
    durationSec: number,
    onProgress: (progress: number, currentTime: number, duration: number) => void,
    onEnded: () => void
  ) {
    this.currentPlayingId = id;
    let elapsed = 0;
    const interval = 50;
    const timer = setInterval(() => {
      if (this.currentPlayingId !== id) {
        clearInterval(timer);
        return;
      }
      elapsed += interval / 1000;
      const prog = Math.min(1, elapsed / durationSec);
      onProgress(prog, elapsed, durationSec);
      if (prog >= 1) {
        clearInterval(timer);
        this.currentPlayingId = null;
        onEnded();
      }
    }, interval);
  }

  seek(progress: number) {
    if (this.currentAudioElement && this.currentAudioElement.duration) {
      this.currentAudioElement.currentTime = progress * this.currentAudioElement.duration;
    }
  }

  stopPlayback() {
    if (this.currentAudioElement) {
      this.currentAudioElement.pause();
      this.currentAudioElement = null;
    }
    this.currentPlayingId = null;
  }

  isPlaying(id: string): boolean {
    return (
      this.currentPlayingId === id &&
      (!this.currentAudioElement || !this.currentAudioElement.paused)
    );
  }

  releaseAll() {
    this.cancelRecording();
    this.stopPlayback();
  }
}

// Generate realistic natural waveform bars
export function generateWaveform(seedStr?: string | null, targetCount = 30): number[] {
  let seed = 42;
  if (seedStr) {
    for (let i = 0; i < seedStr.length; i++) {
      seed = (seed * 31 + seedStr.charCodeAt(i)) % 100000;
    }
  }

  const rng = () => {
    seed = (seed * 9301 + 49297) % 233280;
    return seed / 233280;
  };

  const list: number[] = [];
  let current = 0.25 + rng() * 0.4;
  for (let i = 0; i < targetCount; i++) {
    const delta = (rng() - 0.49) * 0.4;
    current = Math.max(0.1, Math.min(0.95, current + delta));
    list.push(current);
  }
  return list;
}

export function resampleWaveform(samples: number[], targetCount = 30): number[] {
  if (samples.length === 0) return generateWaveform(null, targetCount);
  if (samples.length === targetCount) return samples;

  if (samples.length < targetCount) {
    const result: number[] = [];
    for (let i = 0; i < targetCount; i++) {
      const idx = Math.min(
        samples.length - 1,
        Math.floor((i / (targetCount - 1)) * (samples.length - 1))
      );
      result.push(samples[idx]);
    }
    return result;
  }

  const chunkSize = samples.length / targetCount;
  const result: number[] = [];
  for (let i = 0; i < targetCount; i++) {
    const start = Math.floor(i * chunkSize);
    const end = Math.min(samples.length, Math.floor((i + 1) * chunkSize));
    const sub = samples.slice(start, end);
    const avg = sub.reduce((a, b) => a + b, 0) / (sub.length || 1);
    result.push(Math.max(0.08, Math.min(1.0, avg)));
  }
  return result;
}

// Helper to encode AudioBuffer to simple WAV blob
function audioBufferToWav(buffer: AudioBuffer): Blob {
  const numChannels = buffer.numberOfChannels;
  const sampleRate = buffer.sampleRate;
  const format = 1; // PCM
  const bitDepth = 16;
  const bytesPerSample = bitDepth / 8;
  const blockAlign = numChannels * bytesPerSample;

  const dataLength = buffer.length * blockAlign;
  const bufferLength = 44 + dataLength;

  const arrayBuffer = new ArrayBuffer(bufferLength);
  const view = new DataView(arrayBuffer);

  const writeString = (offset: number, str: string) => {
    for (let i = 0; i < str.length; i++) {
      view.setUint8(offset + i, str.charCodeAt(i));
    }
  };

  writeString(0, 'RIFF');
  view.setUint32(4, 36 + dataLength, true);
  writeString(8, 'WAVE');
  writeString(12, 'fmt ');
  view.setUint32(16, 16, true);
  view.setUint16(20, format, true);
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * blockAlign, true);
  view.setUint16(32, blockAlign, true);
  view.setUint16(34, bitDepth, true);
  writeString(36, 'data');
  view.setUint32(40, dataLength, true);

  // Interleave and write 16-bit PCM samples
  let offset = 44;
  for (let i = 0; i < buffer.length; i++) {
    for (let channel = 0; channel < numChannels; channel++) {
      const sample = Math.max(-1, Math.min(1, buffer.getChannelData(channel)[i]));
      const intSample = sample < 0 ? sample * 0x8000 : sample * 0x7fff;
      view.setInt16(offset, intSample, true);
      offset += 2;
    }
  }

  return new Blob([arrayBuffer], { type: 'audio/wav' });
}
