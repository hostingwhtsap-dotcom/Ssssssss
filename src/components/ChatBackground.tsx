import React, { useEffect, useRef } from 'react';

interface ChatBackgroundProps {
  children: React.ReactNode;
}

export const ChatBackground: React.FC<ChatBackgroundProps> = ({ children }) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const renderDoodles = () => {
      const width = (canvas.width = canvas.parentElement?.clientWidth || window.innerWidth);
      const height = (canvas.height = canvas.parentElement?.clientHeight || window.innerHeight);

      ctx.clearRect(0, 0, width, height);

      // WhatsApp reference soft pink tint
      ctx.fillStyle = '#FDF0F4';
      ctx.fillRect(0, 0, width, height);

      ctx.strokeStyle = 'rgba(216, 27, 96, 0.065)';
      ctx.lineWidth = 1.4;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';

      const cellWidth = 100;
      const cellHeight = 100;

      let row = 0;
      for (let y = 20; y < height + 40; y += cellHeight) {
        const startX = row % 2 === 0 ? 20 : 70;
        let col = 0;
        for (let x = startX; x < width + 40; x += cellWidth) {
          const index = (row * 3 + col) % 8;
          drawDoodle(ctx, index, x, y);
          col++;
        }
        row++;
      }
    };

    renderDoodles();
    window.addEventListener('resize', renderDoodles);
    return () => window.removeEventListener('resize', renderDoodles);
  }, []);

  return (
    <div className="relative w-full h-full flex-1 overflow-hidden bg-[#FDF0F4]">
      <canvas
        ref={canvasRef}
        className="absolute inset-0 pointer-events-none w-full h-full"
      />
      <div className="relative z-10 w-full h-full flex flex-col">{children}</div>
    </div>
  );
};

function drawDoodle(ctx: CanvasRenderingContext2D, index: number, cx: number, cy: number) {
  ctx.save();
  ctx.beginPath();

  switch (index) {
    case 0: {
      // Speech bubble
      ctx.moveTo(cx - 12, cy - 8);
      ctx.lineTo(cx + 12, cy - 8);
      ctx.quadraticCurveTo(cx + 15, cy - 8, cx + 15, cy - 4);
      ctx.lineTo(cx + 15, cy + 5);
      ctx.quadraticCurveTo(cx + 15, cy + 9, cx + 12, cy + 9);
      ctx.lineTo(cx - 4, cy + 9);
      ctx.lineTo(cx - 10, cy + 14);
      ctx.lineTo(cx - 8, cy + 9);
      ctx.lineTo(cx - 12, cy + 9);
      ctx.quadraticCurveTo(cx - 15, cy + 9, cx - 15, cy + 5);
      ctx.lineTo(cx - 15, cy - 4);
      ctx.quadraticCurveTo(cx - 15, cy - 8, cx - 12, cy - 8);
      ctx.stroke();
      break;
    }
    case 1: {
      // Heart
      ctx.moveTo(cx, cy + 6);
      ctx.bezierCurveTo(cx - 10, cy - 2, cx - 10, cy - 9, cx, cy - 3);
      ctx.bezierCurveTo(cx + 10, cy - 9, cx + 10, cy - 2, cx, cy + 6);
      ctx.stroke();
      break;
    }
    case 2: {
      // Clock
      ctx.arc(cx, cy, 10, 0, Math.PI * 2);
      ctx.moveTo(cx, cy);
      ctx.lineTo(cx, cy - 5);
      ctx.moveTo(cx, cy);
      ctx.lineTo(cx + 4, cy);
      ctx.stroke();
      break;
    }
    case 3: {
      // Coffee cup
      ctx.moveTo(cx - 8, cy - 6);
      ctx.lineTo(cx + 6, cy - 6);
      ctx.lineTo(cx + 5, cy + 6);
      ctx.quadraticCurveTo(cx, cy + 9, cx - 6, cy + 6);
      ctx.closePath();
      ctx.stroke();
      // Handle
      ctx.beginPath();
      ctx.arc(cx + 5, cy - 2, 4, 1.5 * Math.PI, 0.5 * Math.PI);
      ctx.stroke();
      break;
    }
    case 4: {
      // Star
      const spikes = 5;
      const outerRadius = 8;
      const innerRadius = 3.5;
      let rot = (Math.PI / 2) * 3;
      const step = Math.PI / spikes;
      ctx.moveTo(cx, cy - outerRadius);
      for (let i = 0; i < spikes; i++) {
        let x = cx + Math.cos(rot) * outerRadius;
        let y = cy + Math.sin(rot) * outerRadius;
        ctx.lineTo(x, y);
        rot += step;
        x = cx + Math.cos(rot) * innerRadius;
        y = cy + Math.sin(rot) * innerRadius;
        ctx.lineTo(x, y);
        rot += step;
      }
      ctx.closePath();
      ctx.stroke();
      break;
    }
    case 5: {
      // Musical note
      ctx.arc(cx - 4, cy + 4, 3.5, 0, Math.PI * 2);
      ctx.moveTo(cx, cy + 4);
      ctx.lineTo(cx, cy - 7);
      ctx.lineTo(cx + 8, cy - 5);
      ctx.lineTo(cx + 8, cy + 2);
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(cx + 4.5, cy + 2, 3.5, 0, Math.PI * 2);
      ctx.stroke();
      break;
    }
    case 6: {
      // Paper plane
      ctx.moveTo(cx - 9, cy - 8);
      ctx.lineTo(cx + 10, cy);
      ctx.lineTo(cx - 9, cy + 8);
      ctx.lineTo(cx - 4, cy);
      ctx.closePath();
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(cx - 4, cy);
      ctx.lineTo(cx + 10, cy);
      ctx.stroke();
      break;
    }
    case 7: {
      // Camera
      ctx.moveTo(cx - 9, cy - 4);
      ctx.lineTo(cx - 4, cy - 4);
      ctx.lineTo(cx - 2, cy - 7);
      ctx.lineTo(cx + 2, cy - 7);
      ctx.lineTo(cx + 4, cy - 4);
      ctx.lineTo(cx + 9, cy - 4);
      ctx.lineTo(cx + 9, cy + 7);
      ctx.lineTo(cx - 9, cy + 7);
      ctx.closePath();
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(cx, cy + 1, 3.5, 0, Math.PI * 2);
      ctx.stroke();
      break;
    }
  }
  ctx.restore();
}
