import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

/**
 * F06 P1/P2a — 계획별 실시간 협업(STOMP over WebSocket). 하나의 STOMP 연결로 처리.
 *
 * 백엔드 `/ws` 로 연결하며 STOMP CONNECT 프레임에 JWT(Authorization: Bearer)를 실어 인증한다.
 * - 프레즌스(P1): `/topic/plans/{id}/presence` 를 구독해 접속자 roster 를 받고, `/app/plans/{id}/join` 으로 입장.
 * - 장소 동기화(P2a): `onPlaceChange` 콜백을 주면 `/topic/plans/{id}/places` 도 같은 연결로 구독한다.
 *
 * @param {number|string} planId 계획 ID
 * @param {{ onPlaceChange?: (event: {actorId:number, actorNickname:string, action:string}) => void }} [options]
 * @returns {{ connected: import('vue').Ref<boolean>, roster: import('vue').Ref<Array>, connect: Function, disconnect: Function }}
 */
export function usePresence(planId, options = {}) {
  const { onPlaceChange } = options
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
        // P2a — 장소 변경 브로드캐스트(원할 때만 같은 연결로 구독).
        if (onPlaceChange) {
          client.subscribe(`/topic/plans/${planId}/places`, (message) => {
            try {
              onPlaceChange(JSON.parse(message.body))
            } catch {
              // 메시지 파싱 실패는 무시(다음 변경 때 다시 동기화됨).
            }
          })
        }
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
