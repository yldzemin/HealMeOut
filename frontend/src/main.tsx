import React from "react";
import ReactDOM from "react-dom/client";
import { MotionConfig } from "motion/react";
import App from "./App";
import { applyTheme, storedTheme } from "./lib/theme";
import "./index.css";

// Before the first paint, so a light-theme user never sees a dark flash (#82). ?theme=light|dark (deep link) wins.
const linked = new URLSearchParams(window.location.search).get("theme");
if (linked === "light" || linked === "dark") applyTheme(linked);
else document.documentElement.dataset.theme = storedTheme();

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    {/* "Reduce motion" in the OS turns screen transitions and movement off (#82). */}
    <MotionConfig reducedMotion="user">
      <App />
    </MotionConfig>
  </React.StrictMode>,
);
