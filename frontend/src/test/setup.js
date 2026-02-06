import { afterAll, afterEach, beforeAll } from 'vitest'
import { cleanup } from '@testing-library/react'
import '@testing-library/jest-dom'
import { server } from './server'

// Start MSW server before all tests
beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }))

// Reset handlers after each test
afterEach(() => {
  server.resetHandlers()
  cleanup()
})

// Close server after all tests
afterAll(() => server.close())
