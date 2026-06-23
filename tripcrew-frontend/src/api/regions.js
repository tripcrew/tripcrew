import { http } from './http'

export const regionApi = {
  listSidos: () => http.get('/regions/sidos').then((response) => response.data),
  listGuguns: (sidoCode) =>
    http.get(`/regions/sidos/${sidoCode}/guguns`).then((response) => response.data),
}
