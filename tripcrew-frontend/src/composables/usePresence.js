import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

/**
 * F06 P1 — 계획별 실시간 프레즌스(STOMP over WebSocket).
 *
 * 백엔드 `/ws` 로 연결하며 STOMP CONNECT 프레임에 JWT(Authorization: Bearer)를 실어 인증한다.
 * `/topic/plans/{id}/presence` 를 구독해 접속자 roster 를 받고, `/app/plans/{id}/join` 으로 입장을 알린다.
 *
 * @returns {{ connected: import('vue').Ref<boolean>, roster: import('vue').Ref<Array>, connect: Function, disconnect: Function }}
 */
export function usePresence(planId) {
  const connected = ref(false)
  const roster = ref([])
  let client = null

  // VITE_API_BASE_URL(http://host/api) → ws://host/ws
  function resolveWsUrl() {
    const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    return base.replace(/^http/, 'ws').replace(/\/api\/?$/, '/ws')
  }

  function connect() {
    const token = localStorage.getItem('tripcrew.accessToken')
    if (!token || client) return

    client = new Client({
      brokerURL: resolveWsUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        connected.value = true
        client.subscribe(`/topic/plans/${planId}/presence`, (message) => {
          try {
            roster.value = JSON.parse(message.body)
          } catch {
            roster.value = []
          }
        })
        client.publish({ destination: `/app/plans/${planId}/join` })
      },
      onWebSocketClose: () => {
        connected.value = false
      },
      onStompError: () => {
        connected.value = false
      },
    })
    client.activate()
  }

  function disconnect() {
    if (client) {
      client.deactivate()
      client = null
    }
    connected.value = false
    roster.value = []
  }

  onUnmounted(disconnect)

  return { connected, roster, connect, disconnect }
}
