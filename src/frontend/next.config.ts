import type { NextConfig } from "next";

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:5000";

// Parse hostname and port from the backend URL for Next.js image domains
const backendUrl = new URL(BACKEND);
const backendHostname = backendUrl.hostname;
const backendPort =
  backendUrl.port || (backendUrl.protocol === "https:" ? "443" : "80");

const nextConfig: NextConfig = {
  // Allow <img> from the backend host
  images: {
    remotePatterns: [
      {
        protocol: backendUrl.protocol.replace(":", "") as "http" | "https",
        hostname: backendHostname,
        port: backendPort,
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;
