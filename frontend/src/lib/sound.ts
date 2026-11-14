// Alarm and confirmation tones with the Web Audio API (no audio files).
// Pattern from the project sound spec: alarm alternates 800 / 1000 Hz every
// 0.4 s; confirmation is a rising 440 → 880 Hz tone. Volume kept moderate.

let ctx: AudioContext | null = null;

function audio(): AudioContext | null {
  try {
    ctx ??= new AudioContext();
    if (ctx.state === "suspended") void ctx.resume();
    return ctx;
  } catch {
    return null; // no audio device or blocked: the UI still shows every alert
  }
}

/** Starts the two-tone alarm. Returns a function that stops it. */
export function startAlarm(): () => void {
  const ac = audio();
  if (!ac) return () => {};
  const gain = ac.createGain();
  gain.gain.value = 0;
  gain.connect(ac.destination);
  const osc = ac.createOscillator();
  osc.type = "square";
  osc.connect(gain);

  // Schedule tones in short batches so the alarm can run for as long as needed.
  const STEP = 0.4;
  let next = ac.currentTime + 0.05;
  let high = false;
  const schedule = () => {
    while (next < ac.currentTime + 2) {
      osc.frequency.setValueAtTime(high ? 1000 : 800, next);
      gain.gain.setValueAtTime(0.06, next);
      gain.gain.setValueAtTime(0, next + STEP - 0.04);
      high = !high;
      next += STEP;
    }
  };
  schedule();
  osc.start();
  const timer = window.setInterval(schedule, 500);

  return () => {
    window.clearInterval(timer);
    const t = ac.currentTime;
    gain.gain.cancelScheduledValues(t);
    gain.gain.setValueAtTime(0, t);
    osc.stop(t + 0.05);
    osc.onended = () => gain.disconnect();
  };
}

/** Short rising confirmation tone, e.g. when the CMO confirms the roles. */
export function playConfirm(): void {
  const ac = audio();
  if (!ac) return;
  const t = ac.currentTime;
  const gain = ac.createGain();
  gain.gain.setValueAtTime(0.0001, t);
  gain.gain.exponentialRampToValueAtTime(0.08, t + 0.03);
  gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.45);
  gain.connect(ac.destination);
  const osc = ac.createOscillator();
  osc.type = "sine";
  osc.frequency.setValueAtTime(440, t);
  osc.frequency.exponentialRampToValueAtTime(880, t + 0.35);
  osc.connect(gain);
  osc.start(t);
  osc.stop(t + 0.5);
  osc.onended = () => gain.disconnect();
}
