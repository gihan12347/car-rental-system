/**
 * Minimal service worker — enables Chrome “Install app” / standalone window.
 * Does not cache pages; always uses the live server.
 */
self.addEventListener("install", function (event) {
  self.skipWaiting();
});

self.addEventListener("activate", function (event) {
  event.waitUntil(self.clients.claim());
});

self.addEventListener("fetch", function () {});
