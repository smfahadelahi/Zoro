/** End-to-end checks run against the HTML file (HF responses are intercepted fixtures). */
const { chromium } = require("playwright");
const chromiumModule = require("@sparticuz/chromium");
const bundledChromium = chromiumModule.default || chromiumModule;
const assert = require("node:assert/strict");
const path = require("node:path");
const fs = require("node:fs");

(async () => {
  const browser = await chromium.launch({
    executablePath: await bundledChromium.executablePath(),
    args: bundledChromium.args.filter(
      (arg) => !arg.includes("disable-web-security"),
    ),
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
  // Real fetch paths with deterministic API responses; no external service dependency.
  await page.route("https://huggingface.co/api/models**", async (route) => {
    const url = new URL(route.request().url());
    const q = url.searchParams.get("search");
    if (q === "offline") return route.abort("internetdisconnected");
    if (q === "server-error")
      return route.fulfill({ status: 503, body: "Unavailable" });
    const headers = {
      "access-control-allow-origin": "*",
      "content-type": "application/json",
    };
    const reply = (data) =>
      route.fulfill({ headers, body: JSON.stringify(data) });
    if (q !== null) {
      assert.equal(url.searchParams.get("filter"), "gguf");
      assert.equal(url.searchParams.get("sort"), "downloads");
      assert.equal(url.searchParams.get("limit"), "20");
      if (q === "no-such-repo") return reply([]);
      await new Promise((resolve) =>
        setTimeout(resolve, q === "stale" ? 800 : 100),
      );
      return reply(
        q === "stale"
          ? [{ id: "old/result" }]
          : [
              { id: "test/llama-real-repo", downloads: 123456, likes: 789 },
              { id: "test/empty-repo", downloads: 42, likes: 1 },
              { id: "test/error-repo", downloads: 10, likes: 0 },
            ],
      );
    }
    assert.equal(url.searchParams.get("blobs"), "true");
    if (url.pathname.endsWith("error-repo"))
      return route.fulfill({ status: 503, body: "Unavailable", headers });
    if (url.pathname.endsWith("empty-repo"))
      return reply({ siblings: [{ rfilename: "README.md", size: 123 }] });
    await new Promise((resolve) => setTimeout(resolve, 100));
    return reply({
      siblings: [
        { rfilename: "README.md", size: 123 },
        { rfilename: "model-Q4_K_M.gguf", size: 800000000 },
        { rfilename: "nested/model-Q5_K_M.gguf", size: 1100000000 },
        { rfilename: "model-Q8_0.GGUF", lfs: { size: 1600000000 } },
        { rfilename: "missing-size.gguf" },
      ],
    });
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

  // Exercise both content-resizing and visual-only keyboard behavior.
  await page.locator("#message-input").focus();
  await page.setViewportSize({ width: 360, height: 480 });
  await page.waitForFunction(() => state.keyboardOpen);
  await page.waitForTimeout(350);
  assert.equal(await page.locator(".suggestions").isVisible(), false);
  let box = await page.locator("#message-input").boundingBox();
  assert.ok(box.y >= 0 && box.y + box.height <= 480);
  await page.locator("#message-input").fill("Keyboard text stays visible");
  assert.equal(
    await page.locator("#message-input").inputValue(),
    "Keyboard text stays visible",
  );
  await page.setViewportSize({ width: 360, height: 800 });
  await page.waitForFunction(() => !state.keyboardOpen);
  assert.equal(await page.locator(".suggestions").isVisible(), true);
  await page.evaluate(() => {
    Object.defineProperty(window.visualViewport, "height", {
      configurable: true,
      get: () => 450,
    });
    window.visualViewport.dispatchEvent(new Event("resize"));
  });
  await page.waitForFunction(() => state.keyboardOpen);
  box = await page.locator("#message-input").boundingBox();
  assert.ok(box.y >= 0 && box.y + box.height <= 450);
  await page.evaluate(() => {
    delete window.visualViewport.height;
    window.visualViewport.dispatchEvent(new Event("resize"));
  });
  await page.waitForFunction(() => !state.keyboardOpen);
  await page.locator("#message-input").fill("");
  assert.deepEqual(network, []);
  console.log(
    "PASS keyboard shrink, visual viewport, footer visibility and suggestion restore",
  );

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
  assert.equal(await has("No GGUF models found"), true);
  for (const q of ["offline", "server-error"]) {
    await page.locator("#hf-search").fill(q);
    await page.locator("#hf-search").press("Enter");
    await page.waitForFunction(() => !state.hfLoading);
    assert.equal(
      await has("Search failed. Check your internet connection."),
      true,
    );
  }
  await page.locator("#hf-search").fill("stale");
  await page.locator("#hf-search").press("Enter");
  await page.waitForFunction(() => state.hfLoading);
  assert.equal(await page.locator(".spinner").count(), 1);
  await page.locator("#hf-search").fill("llama & tiny");
  await page.locator("#search-form button").click();
  await page.waitForFunction(() => !state.hfLoading);
  assert.equal(await page.locator(".repo").count(), 3);
  await page.waitForTimeout(900);
  assert.equal(await page.locator(".repo").count(), 3); // stale results must not overwrite
  for (const [index, expected] of [
    [1, "No GGUF models found"],
    [2, "Search failed. Check your internet connection."],
  ]) {
    await page
      .locator(`[data-action="open-repo"][data-index="${index}"]`)
      .click();
    await page.waitForFunction(() => !state.filesLoading);
    assert.equal(await has(expected), true);
    await click("back");
  }
  await click("open-repo");
  await page.waitForFunction(() => !state.filesLoading);
  assert.equal(await page.locator(".model-card").count(), 4);
  assert.equal(await page.evaluate(() => state.files[0].size), 0.8);
  assert.equal(
    await page.evaluate(() => state.files[1].name),
    "nested/model-Q5_K_M.gguf",
  );
  assert.equal(await page.evaluate(() => state.files[2].size), 1.6);
  assert.equal(
    await page.locator('[data-action="download-model"]:disabled').count(),
    1,
  );
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
    "PASS API search/Enter, metadata sizes, errors, empty/files, stale requests and simulated download",
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
  assert.ok(
    network.length > 0 &&
      network.every((url) =>
        url.startsWith("https://huggingface.co/api/models"),
      ),
  );
  console.log(
    "PASS denied localStorage, zero console exceptions, only requested HF metadata traffic",
  );
  await browser.close();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
