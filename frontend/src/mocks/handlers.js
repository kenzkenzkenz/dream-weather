import { http } from 'msw'

const mockMatch = {
  data: {
    slug: "boyne-mountain-resort",
    title: "Boyne Mountain Resort Clock Tower",
    country: { name: "USA" },
    description: "Snowy winter view from the top of Boyne Mountain",
    forecast: {
      shortForecast: "Snow showers",
      temperature: 28,
    },
    stream_type: "youtube",
    stream_url: "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
  }
}

export const handlers = [
  // Wakeup endpoint
  http.get('/api/v1/wakeup', async (info) => {
    await new Promise((r) => setTimeout(r, 2500))
    return new Response(JSON.stringify({ ok: true }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  }),

  // Match endpoint
  http.post('/api/v1/livestream/match', async (info) => {
    // Optional: inspect request body
    try {
      const body = await info.request.json().catch(() => ({}))
    } catch (e) {
      // ignore
    }
    await new Promise((r) => setTimeout(r, 1500))
    return new Response(JSON.stringify(mockMatch), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  }),

  // Report endpoint
  http.post('/api/v1/livestream/report', async (info) => {
    await new Promise((r) => setTimeout(r, 300))
    return new Response(JSON.stringify({ ok: true }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  }),
]
