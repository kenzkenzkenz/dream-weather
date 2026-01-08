export const getMessages = (isServerAwake) => {
  let msgs = [];
  if (!isServerAwake) {
    msgs.push("Waking up server...");
  }
  msgs.push(
    "Fetching locations…",
    "Checking the forecast…",
    "Finding the perfect webcam…",
    "Almost there…"
  );
  return msgs;
};