/**
 * Catch-all proxy route — forwards all API requests to the Spring Boot backend
 * and faithfully passes through ALL response headers, including Set-Cookie.
 *
 * Next.js rewrites silently strip Set-Cookie headers, which breaks session-based
 * auth. This route handler avoids that by controlling the response explicitly.
 *
 * Mounted at: /api/proxy/[...path]
 * Frontend api.ts maps:  /auth/... → /api/proxy/auth/...  etc.
 */

import { NextRequest, NextResponse } from "next/server";

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:5000";

// Headers that must not be forwarded to the backend (hop-by-hop / Next internals)
const DROP_REQUEST_HEADERS = new Set([
  "host",
  "connection",
  "keep-alive",
  "transfer-encoding",
  "te",
  "upgrade",
  "proxy-authorization",
  "proxy-authenticate",
]);

// Headers that must not be copied from the backend response to the browser
const DROP_RESPONSE_HEADERS = new Set([
  "transfer-encoding",
  "connection",
  "keep-alive",
]);

async function handler(
  req: NextRequest,
  { params }: { params: Promise<{ path: string[] }> },
): Promise<NextResponse> {
  const { path } = await params;

  // Reconstruct the upstream path: strip leading /api/proxy, keep the rest.
  // Next.js decodes path params before handing them to the handler, so we must
  // re-encode each segment to produce a valid URL for the upstream fetch call.
  const upstreamPath = "/" + path.map((s) => encodeURIComponent(s)).join("/");
  const search = req.nextUrl.search ?? "";
  const upstreamUrl = `${BACKEND}${upstreamPath}${search}`;

  // Copy request headers, dropping hop-by-hop headers
  const forwardHeaders = new Headers();
  req.headers.forEach((value, key) => {
    if (!DROP_REQUEST_HEADERS.has(key.toLowerCase())) {
      forwardHeaders.set(key, value);
    }
  });

  // Forward the request body for non-GET/HEAD methods
  const hasBody = req.method !== "GET" && req.method !== "HEAD";

  let upstreamRes: Response;
  try {
    upstreamRes = await fetch(upstreamUrl, {
      method: req.method,
      headers: forwardHeaders,
      body: hasBody ? req.body : undefined,
      // Required to stream the request body
      // @ts-expect-error — Node fetch duplex option
      duplex: "half",
      credentials: "include",
    });
  } catch (err) {
    console.error("[proxy] upstream fetch failed", upstreamUrl, err);
    return NextResponse.json(
      { error: "Upstream unavailable" },
      { status: 502 },
    );
  }

  // Read body as ArrayBuffer to preserve binary data
  const body = await upstreamRes.arrayBuffer();

  // Build response, forwarding all upstream headers
  const res = new NextResponse(body.byteLength > 0 ? body : null, {
    status: upstreamRes.status,
    statusText: upstreamRes.statusText,
  });

  upstreamRes.headers.forEach((value, key) => {
    if (!DROP_RESPONSE_HEADERS.has(key.toLowerCase())) {
      res.headers.append(key, value);
    }
  });

  return res;
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;
export const OPTIONS = handler;
export const HEAD = handler;
