/** End-to-end checks run directly against the single offline file. */
const { chromium } = require("playwright");
const chromiumModule = require("@sparticuz/chromium");
const bundledChromium = chromiumModule.default || chromiumModule;
const assert = require("node:assert/strict");
const path = require("node:path");
const fs = require("node:fs");

(async () => {
  const browser = await chromium.launch({
    executablePath: await bundledChromium.executablePath(),
    args: bundledChromium.args,
    headless: true,
  });
  const context = await browser.newContext({
    viewport: { width: 360, height: 800 },
    reducedMotion: "reduce",
  });
  const page = await context.newPage();
  const errors = [],
    network = [];
  page.on("pageerror", (error) => errors.push(error.message));
  page.on("request", (req) => {
    if (/^https?:/.test(req.url())) network.push(req.url());
  });
  const file = "file://" + path.resolve(__dirname, "../index.html");
  const click = (a) => page.locator(`[data-action="${a}"]`).first().click();
  const done = () => page.waitForFunction(() => !state.stream);
  const send = async (text) => {
    await page.locator("#message-input").fill(text);
    await click("send");
    await done();
  };
  const has = (text) =>
    page.getByText(text, { exact: true }).first().isVisible();
  fs.mkdirSync(path.resolve(__dirname, "../test-results"), { recursive: true });

  await page.goto(file);
  assert.equal(await page.locator(".onboarding").count(), 1);
  await page.screenshot({ path: "test-results/onboarding.png" });
  await click("onboard-next");
  await click("onboard-next");
  await click("onboard-next");
  assert.equal(await page.locator(".model-card").count(), 5);
  await click("close-overlay");
  await page.screenshot({ path: "test-results/home.png" });
  assert.equal(await page.locator(".suggestion").count(), 4);
  assert.equal(
    await page.evaluate(() => document.querySelector("#phone").scrollWidth),
    360,
  );
  console.log("PASS onboarding, model picker, 360×800 layout");

  await page.locator("#message-input").fill("Set a timer for 3 seconds");
  await click("send");
  assert.equal(await page.locator('[data-action="stop"]').count(), 1);
  await done();
  await click("confirm-action");
  assert.equal(await page.evaluate(() => state.timers.length), 1);
  await page.waitForFunction(() => state.timers[0].fired);
  await page.screenshot({ path: "test-results/chat-action.png" });
  await send("Set an alarm for 7:00 AM");
  await click("confirm-action");
  assert.equal(await page.evaluate(() => state.alarms[0].time), "7:00 AM");
  await send("Save a note: <img src=x onerror=alert(1)> Buy tea");
  await click("confirm-action");
  assert.equal(await page.locator("img").count(), 0);
  await click("plus");
  await click("notes");
  assert.equal(await page.locator(".note").count(), 1);
  console.log("PASS streamed reply, timer completion, alarm, safe saved note");

  await click("back");
  await click("new-chat");
  await send("hello");
  await click("history");
  assert.equal(await page.locator(".history-item").count(), 2);
  await page.locator("#history-search").fill("timer");
  assert.equal(await page.locator(".history-item").count(), 1);
  await click("open-chat");
  assert.equal(await page.locator(".message.user").count(), 3);
  await send("Set a timer for 10 minutes");
  await click("cancel-action");
  assert.equal(
    await page.evaluate(() => chat().messages.at(-1).action.status),
    "cancelled",
  );
  await click("regenerate"); // Completed action cannot execute twice.
  assert.equal(await page.evaluate(() => state.timers.length), 1);
  console.log(
    "PASS session history, search, reopen, action cancellation, duplicate guard",
  );

  await click("settings");
  await page.locator('[data-key="ask"]').click();
  await page.locator('[data-key="tool:alarm"]').click();
  await page.locator('[data-action="tone"][data-value="Concise"]').click();
  await page.locator("#system-prompt").fill("Be considerate and brief.");
  await click("add-fact");
  await page.locator("#modal-text").fill("I enjoy green tea.");
  await click("save-text");
  await click("back");
  await send("Save a note: automatic note");
  assert.equal(await page.evaluate(() => state.notes.length), 2);
  assert.equal(
    await page.evaluate(() => chat().messages.at(-1).action.status),
    "done",
  );
  await send("Set an alarm for 8:00 AM");
  assert.equal(await page.evaluate(() => chat().messages.at(-1).action), null);
  await send("What do you remember?");
  assert.match(
    await page.locator(".assistant .bubble").last().innerText(),
    /green tea/,
  );
  await page.locator("#message-input").fill("Explain quantum computing simply");
  await click("send");
  await page.waitForTimeout(550);
  await click("stop");
  assert.equal(await page.evaluate(() => state.stream), null);
  console.log(
    "PASS auto actions, tool permissions, prompt, tone, memory, stop",
  );

  await click("models");
  await page.locator('[data-action="download-model"][data-id="qwen3"]').click();
  await page.waitForFunction(() => state.downloads[0].progress > 0);
  await click("pause-download");
  const progress = await page.evaluate(() => state.downloads[0].progress);
  await page.waitForTimeout(1200);
  assert.equal(
    await page.evaluate(() => state.downloads[0].progress),
    progress,
  );
  await click("pause-download");
  await click("back");
  await page.waitForFunction((p) => state.downloads[0].progress > p, progress);
  // Advance long download near completion, exercising the real completion path.
  await page.evaluate(() => {
    state.downloads[0].progress = 99.99;
  });
  await page.waitForFunction(
    () => state.models.find((m) => m.id === "qwen3").downloaded,
  );
  await click("models");
  await page.locator('[data-action="select-model"][data-id="qwen3"]').click();
  assert.equal(await page.evaluate(() => state.activeModel), "qwen3");
  await click("models");
  await page.locator('[data-action="download-model"][data-id="qwen7"]').click();
  await page.evaluate(() => {
    state.downloads.find((d) => d.modelId === "qwen7").progress = 99.99;
  });
  await page.waitForFunction(
    () => state.models.find((m) => m.id === "qwen7").downloaded,
  );
  await click("models");
  await page.locator('[data-action="select-model"][data-id="qwen7"]').click();
  assert.equal(await has("Not enough RAM for this model"), true);
  assert.equal(await page.evaluate(() => state.activeModel), "qwen3");
  console.log(
    "PASS download progress, pause/resume, background completion, selection, RAM error",
  );

  await click("hf");
  assert.equal(await has("Search to find downloadable GGUF models."), true);
  await page.locator("#hf-search").fill("no-such-repo");
  await page.locator("#search-form button").click();
  await page.waitForFunction(() => !state.hfLoading);
  assert.equal(await has("No models found"), true);
  await page.locator("#hf-search").fill("llama");
  await page.locator("#search-form button").click();
  await page.waitForFunction(() => !state.hfLoading);
  assert.equal(await page.locator(".repo").count(), 3);
  await click("open-repo");
  assert.equal(await page.locator(".model-card").count(), 3);
  await click("download-model");
  assert.equal(
    await page.evaluate(() => state.downloads.at(-1).status),
    "running",
  );
  await click("cancel-download");
  assert.equal(
    await page.evaluate(
      () => state.downloads.filter((d) => d.status !== "done").length,
    ),
    0,
  );
  console.log(
    "PASS mock community search, loading/empty/results, variants, cancel",
  );

  await click("back");
  await click("settings");
  await page.locator("#language").selectOption("bn");
  assert.equal(await page.locator("html").getAttribute("lang"), "bn");
  assert.equal(await has("সেটিংস"), true);
  await page.screenshot({ path: "test-results/bangla-settings.png" });
  await page.locator("#language").selectOption("en");
  await page.locator('[data-key="lock"]').click();
  await page.locator("#pin-setup").fill("12");
  await click("save-pin");
  assert.equal(await has("PIN must be exactly 4 digits."), true);
  await page.locator("#pin-setup").fill("1234");
  await click("save-pin");
  await click("lock-now");
  await page.locator("#unlock-pin").fill("9999");
  await click("unlock");
  assert.equal(await has("Incorrect PIN. Try again."), true);
  await page.locator("#unlock-pin").fill("1234");
  await click("unlock");
  await page.reload();
  assert.equal(await page.locator(".lock-screen").count(), 1);
  await page.locator("#unlock-pin").fill("1234");
  await click("unlock");
  assert.equal(await page.evaluate(() => state.onboardDone), true);
  assert.equal(await page.evaluate(() => state.notes.length), 2);
  assert.equal(await page.evaluate(() => state.facts.length), 1);
  assert.equal(await page.evaluate(() => state.activeModel), "qwen3");
  assert.equal(await page.evaluate(() => state.chats.length), 0);
  assert.equal(
    await page.evaluate(() => state.settings.prompt),
    "Be considerate and brief.",
  );
  console.log(
    "PASS Bangla UI, PIN validation, lock/reload, durable state and session-only chats",
  );

  await click("settings");
  await click("delete-all");
  await click("close-overlay");
  assert.equal(await page.evaluate(() => state.notes.length), 2);
  await click("delete-all");
  await click("reset");
  assert.equal(
    await page.evaluate(
      () =>
        state.notes.length +
        state.facts.length +
        state.chats.length +
        state.downloads.length,
    ),
    0,
  );
  assert.equal(await page.evaluate(() => state.activeModel), null);
  await page.setViewportSize({ width: 1280, height: 1000 });
  await page.screenshot({ path: "test-results/desktop.png" });
  assert.ok(
    await page.evaluate(
      () =>
        document.querySelector("#phone").getBoundingClientRect().width <= 400,
    ),
  );
  console.log("PASS destructive reset, confirmation, desktop phone frame");

  // Denied browser storage must not break first run or in-memory operation.
  const blocked = await context.newPage();
  blocked.on("pageerror", (err) => errors.push(err.message));
  await blocked.addInitScript(() => {
    Object.defineProperty(window, "localStorage", {
      get() {
        throw new DOMException("Denied", "SecurityError");
      },
    });
  });
  await blocked.goto(file);
  await blocked.locator('[data-action="onboard-skip"]').click();
  assert.equal(await blocked.locator(".suggestion").count(), 4);
  await blocked.locator('[data-action="settings"]').click();
  await blocked.locator("#language").selectOption("bn");
  assert.equal(await blocked.evaluate(() => state.settings.language), "bn");
  assert.deepEqual(errors, []);
  assert.deepEqual(network, []);
  console.log(
    "PASS denied localStorage, zero console exceptions, zero network requests",
  );
  await browser.close();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
