import { useState } from "react";

/** Dark is the default; light is optional (#82). The choice is remembered on this device. */
export type Theme = "dark" | "light";

const KEY = "astra-twin-theme";

export function storedTheme(): Theme {
  try {
    return localStorage.getItem(KEY) === "light" ? "light" : "dark";
  } catch {
    return "dark"; // storage blocked (private window, previews): dark, as before
  }
}

/** Sets the theme on <html data-theme>, which switches the tokens in index.css. */
export function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme;
  try {
    localStorage.setItem(KEY, theme);
  } catch {
    // not remembered, but still applied
  }
}

export function useTheme(): [Theme, (t: Theme) => void] {
  const [theme, setTheme] = useState<Theme>(() => (document.documentElement.dataset.theme === "light" ? "light" : "dark"));
  return [
    theme,
    (t: Theme) => {
      applyTheme(t);
      setTheme(t);
    },
  ];
}
