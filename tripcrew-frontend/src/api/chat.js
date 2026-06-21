import { http } from './http'

export const chatApi = {
  send: (payload) => http.post('/chat/messages', payload).then((r) => r.data),
}
