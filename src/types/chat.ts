export type MessageStatus = 'SENT' | 'DELIVERED' | 'READ';

export type MessageAttachmentType =
  | 'NONE'
  | 'IMAGE'
  | 'VIDEO'
  | 'VOICE_NOTE'
  | 'AUDIO'
  | 'DOCUMENT';

export interface ChatMessage {
  id: string;
  text: string;
  timestamp: string;
  isOutgoing: boolean;
  status: MessageStatus;
  attachmentType: MessageAttachmentType;
  mediaPath?: string | null;
  thumbnailPath?: string | null;
  attachmentName?: string | null;
  attachmentSize?: string | null;
  audioDuration?: string | null;
  waveform: number[];
}

export interface SharedContentItem {
  id: string;
  file?: File | Blob | null;
  dataUrl: string;
  type: MessageAttachmentType;
  fileName: string;
  sizeOrDuration: string;
  isVideo: boolean;
  thumbnailUrl?: string | null;
}

export type ActiveCallState = 'NONE' | 'AUDIO' | 'VIDEO';

export interface PendingMediaPreview {
  fileOrUrl: string;
  isVideo: boolean;
  duration?: string;
  thumbnailUrl?: string | null;
  fileName?: string;
  fileSize?: string;
}

export interface DeviceAudioItem {
  id: string;
  displayName: string;
  title: string;
  artist: string;
  durationFormatted: string;
  sizeFormatted: string;
  dataUrl?: string;
}
