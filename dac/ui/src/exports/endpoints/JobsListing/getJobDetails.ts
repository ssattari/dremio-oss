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

import { getApiContext } from "dremio-ui-common/contexts/ApiContext.js";
import { JobDetails } from "@app/exports/types/JobDetails.type";

const getJobDetailsUrl = (jobId: string) =>
  getApiContext().createSonarUrl(`jobs-listing/v1.0/${jobId}/jobDetails`);

export const getJobDetails = (jobId: string): Promise<JobDetails> =>
  getApiContext()
    .fetch(getJobDetailsUrl(jobId))
    .then((res: Response) => res.json());