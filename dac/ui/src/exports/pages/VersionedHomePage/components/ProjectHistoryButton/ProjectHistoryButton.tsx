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

import { Button } from "dremio-ui-lib/components";
import { Link } from "react-router";
import { FormattedMessage } from "react-intl";

import * as headerClasses from "#oss/exports/pages/VersionedHomePage/components/VersionedPageTableHeader/VersionedPageTableHeader.module.less";

const ProjectHistoryButton = (props: {
  to: string | Record<string, unknown>;
}) => (
  <Button
    variant="secondary"
    as={Link}
    to={props.to}
    className={headerClasses["versioned-page-table-header__header-button"]}
  >
    <dremio-icon name="interface/history" alt="history" />
    <FormattedMessage id="Common.History" />
  </Button>
);

export default ProjectHistoryButton;
