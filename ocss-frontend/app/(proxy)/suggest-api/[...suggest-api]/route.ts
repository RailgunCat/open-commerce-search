import { NextResponse } from "next/server"
import { env } from "@/env.mjs"

export async function GET(request: Request) {
  if (!env.SEARCH_API_URL) {
    return NextResponse.error()
  }

  const requestURL = new URL(request.url)
  const res = await fetch(
    `${env.SUGGEST_API_URL}${requestURL.pathname}${requestURL.search}`,
    {
      headers: {
        Authorization: `Basic ${env.SUGGEST_API_AUTH}`,
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    }
  )

  if (res.status !== 200) {
    return NextResponse.error()
  }

  const data = await res.json()

  return NextResponse.json(data)
}
