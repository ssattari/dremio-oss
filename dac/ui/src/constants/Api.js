/*
 * Copyright (C) 2017-2019 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const host = window.location.host;
const isSecure = window.location.protocol === "https:";

export const API_URL = `//${host}/apiv1`;
export const API_URL_V2 = `//${host}/apiv2`;
export const API_V2 = "apiv2";
export const API_URL_V3 = `//${host}/api/v3`;
export const API_V3 = "api/v3";
export const WEB_SOCKET_URL = `ws${isSecure ? "s" : ""}:${API_URL_V2}/socket`;
export const NESSIE_PROXY_URL_V2 = `//${host}/nessie-proxy/v2`;

class Api {
  toString() {
    console.warn(
      "Default import deprecated, use { API_URL } from '@app/constants/Api.js'" +
        " notation instead of API_URL from '@app/constants/Api'",
    );
    return API_URL;
  }
}

const api = new Api();

export default api;
