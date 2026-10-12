import React, { useState, useEffect, useRef } from 'react';
import {
  PhoneOff,
  Mic,
  MicOff,
  Volume2,
  Video,
  VideoOff,
  Share2,
  SwitchCamera,
  ShieldCheck,
} from 'lucide-react';

export const AudioCallScreen: React.FC<{ onEndCall: () => void }> = ({ onEndCall }) => {
  const [isMuted, setIsMuted] = useState(false);
  const [isSpeakerOn, setIsSpeakerOn] = useState(false);
  const [callDuration, setCallDuration] = useState(0);
  const [callStatus, setCallStatus] = useState('Calling...');

  useEffect(() => {
    const t1 = setTimeout(() => setCallStatus('Ringing...'), 1200);
    const t2 = setTimeout(() => {
      setCallStatus('Connected');
    }, 2800);

    const interval = setInterval(() => {
      setCallDuration((prev) => prev + 1);
    }, 1000);

    return () => {
      clearTimeout(t1);
      clearTimeout(t2);
      clearInterval(interval);
    };
  }, []);

  const formatTimer = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m < 10 ? '0' : ''}${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-[#0B141B] text-white flex flex-col justify-between p-6 select-none animate-fade-in"
      data-testid="audio_call_screen"
    >
      {/* Top Section */}
      <div className="flex flex-col items-center mt-12">
        <h2 className="text-2xl font-bold">Apex Customer Care</h2>
        <p className="text-sm text-[#8696A0] mt-2">
          {callStatus === 'Connected' ? formatTimer(callDuration) : callStatus}
        </p>
      </div>

      {/* Center Avatar */}
      <div className="flex justify-center">
        <div className="w-32 h-32 rounded-full overflow-hidden bg-white/10 p-1 border-2 border-white/20 shadow-2xl">
          <img
            src="/company_logo.jpg"
            alt="Apex Avatar"
            className="w-full h-full rounded-full object-cover"
          />
        </div>
      </div>

      {/* Bottom Controls */}
      <div className="flex items-center justify-around pb-10">
        <CallActionBtn
          icon={isMuted ? <MicOff className="w-6 h-6" /> : <Mic className="w-6 h-6" />}
          label={isMuted ? 'Unmute' : 'Mute'}
          isActive={isMuted}
          onClick={() => setIsMuted(!isMuted)}
        />

        <CallActionBtn
          icon={<Volume2 className="w-6 h-6" />}
          label="Speaker"
          isActive={isSpeakerOn}
          onClick={() => setIsSpeakerOn(!isSpeakerOn)}
        />

        {/* End Call Button */}
        <button
          onClick={onEndCall}
          className="w-16 h-16 rounded-full bg-[#E53935] hover:bg-red-700 active:scale-95 transition flex items-center justify-center text-white shadow-xl cursor-pointer"
          data-testid="end_audio_call_button"
        >
          <PhoneOff className="w-7 h-7" />
        </button>
      </div>
    </div>
  );
};

export const VideoCallScreen: React.FC<{
  onEndCall: () => void;
  onScreenShareToggled?: (isSharing: boolean) => void;
}> = ({ onEndCall, onScreenShareToggled }) => {
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [callDuration, setCallDuration] = useState(0);
  const [callStatus, setCallStatus] = useState('Connecting Video...');
  const localVideoRef = useRef<HTMLVideoElement | null>(null);
  const localStreamRef = useRef<MediaStream | null>(null);

  useEffect(() => {
    const t = setTimeout(() => setCallStatus('Connected'), 1500);
    const interval = setInterval(() => setCallDuration((d) => d + 1), 1000);

    // Initialize camera stream if supported
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      navigator.mediaDevices
        .getUserMedia({ video: true, audio: false })
        .then((stream) => {
          localStreamRef.current = stream;
          if (localVideoRef.current) {
            localVideoRef.current.srcObject = stream;
          }
        })
        .catch(() => {
          // Camera permission or device fallback
        });
    }

    return () => {
      clearTimeout(t);
      clearInterval(interval);
      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach((t) => t.stop());
      }
    };
  }, []);

  const toggleScreenShare = () => {
    const nextState = !isScreenSharing;
    setIsScreenSharing(nextState);
    onScreenShareToggled?.(nextState);
  };

  const formatTimer = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m < 10 ? '0' : ''}${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-[#0F1B21] text-white flex flex-col justify-between p-4 select-none animate-fade-in"
      data-testid="video_call_screen"
    >
      {/* Top Bar: Security badge & Camera Switch */}
      <div className="flex items-center justify-between z-20 pt-4 px-2">
        <div className="flex items-center gap-2 bg-black/60 rounded-full px-3 py-1.5 backdrop-blur-xs text-xs">
          <ShieldCheck className="w-3.5 h-3.5 text-[#00A884]" />
          <span>{isScreenSharing ? 'Screen Sharing' : 'End-to-End Encrypted'}</span>
        </div>

        <button
          className="w-10 h-10 rounded-full bg-black/60 flex items-center justify-center text-white hover:bg-black/80 transition cursor-pointer"
          data-testid="switch_camera_button"
        >
          <SwitchCamera className="w-5 h-5" />
        </button>
      </div>

      {/* Main Remote View / Screen Sharing Area */}
      <div className="flex-1 flex flex-col items-center justify-center relative my-4">
        {isScreenSharing ? (
          <div className="bg-[#00A884]/95 text-white p-6 rounded-2xl flex flex-col items-center max-w-sm text-center shadow-2xl">
            <Share2 className="w-10 h-10 mb-2" />
            <h3 className="text-lg font-bold">You are sharing your screen</h3>
            <p className="text-xs text-white/90 mt-1">
              The support agent can now view your screen.
            </p>
          </div>
        ) : (
          <div className="flex flex-col items-center">
            <div className="w-28 h-28 rounded-full overflow-hidden border-2 border-white/20 mb-4 shadow-2xl">
              <img
                src="/company_logo.jpg"
                alt="Apex Video Stream"
                className="w-full h-full object-cover"
              />
            </div>
            <h3 className="text-xl font-bold">Apex Customer Care</h3>
            <p className="text-sm text-[#8696A0] mt-1">
              {callStatus === 'Connected' ? formatTimer(callDuration) : callStatus}
            </p>
          </div>
        )}

        {/* Picture-in-Picture Local Camera Inset (Top-Right) */}
        <div
          className="absolute top-2 right-2 w-28 h-40 bg-black rounded-xl overflow-hidden border border-white/30 shadow-2xl z-20 flex items-center justify-center"
          data-testid="local_camera_preview"
        >
          {isVideoEnabled ? (
            <video
              ref={localVideoRef}
              autoPlay
              playsInline
              muted
              className="w-full h-full object-cover scale-x-[-1]"
            />
          ) : (
            <div className="flex flex-col items-center text-[#8696A0]">
              <VideoOff className="w-6 h-6 mb-1" />
              <span className="text-[10px]">Video Off</span>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Controls */}
      <div className="flex items-center justify-around pb-8 z-20">
        <CallActionBtn
          icon={<Share2 className="w-5 h-5" />}
          label={isScreenSharing ? 'Stop Share' : 'Share Screen'}
          isActive={isScreenSharing}
          onClick={toggleScreenShare}
        />

        <CallActionBtn
          icon={isMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
          label={isMuted ? 'Unmute' : 'Mute'}
          isActive={isMuted}
          onClick={() => setIsMuted(!isMuted)}
        />

        <CallActionBtn
          icon={isVideoEnabled ? <Video className="w-5 h-5" /> : <VideoOff className="w-5 h-5" />}
          label={isVideoEnabled ? 'Cam Off' : 'Cam On'}
          isActive={!isVideoEnabled}
          onClick={() => setIsVideoEnabled(!isVideoEnabled)}
        />

        <button
          onClick={onEndCall}
          className="w-14 h-14 rounded-full bg-[#E53935] hover:bg-red-700 active:scale-95 transition flex items-center justify-center text-white shadow-xl cursor-pointer"
          data-testid="end_video_call_button"
        >
          <PhoneOff className="w-6 h-6" />
        </button>
      </div>
    </div>
  );
};

const CallActionBtn: React.FC<{
  icon: React.ReactNode;
  label: string;
  isActive: boolean;
  onClick: () => void;
}> = ({ icon, label, isActive, onClick }) => {
  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-1.5 focus:outline-none cursor-pointer"
    >
      <div
        className={`w-12 h-12 rounded-full flex items-center justify-center transition ${
          isActive ? 'bg-[#00A884] text-white' : 'bg-white/20 text-white hover:bg-white/30'
        }`}
      >
        {icon}
      </div>
      <span className="text-[11px] text-white/90">{label}</span>
    </button>
  );
};
