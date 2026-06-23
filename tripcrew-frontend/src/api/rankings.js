import { http } from './http'

export const rankingApi = {
  getAttractions: () => http.get('/rankings/attractions').then((response) => response.data),
}
