import { http } from './http'

export const activityApi = {
  getRecent: () => http.get('/activities/recent').then((response) => response.data),
}
