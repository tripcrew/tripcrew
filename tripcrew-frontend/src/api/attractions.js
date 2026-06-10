import { http } from './http'

function serializeParams(params) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === null || value === undefined || value === '') return

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== null && item !== undefined && item !== '') {
          searchParams.append(key, item)
        }
      })
      return
    }

    searchParams.append(key, value)
  })

  return searchParams.toString()
}

export const attractionApi = {
  search: (params) =>
    http
      .get('/attractions', {
        params,
        paramsSerializer: { serialize: serializeParams },
      })
      .then((r) => r.data),
  get: (no) => http.get(`/attractions/${no}`).then((r) => r.data),
}
