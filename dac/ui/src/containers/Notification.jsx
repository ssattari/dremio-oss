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
import { Component, createRef } from "react";
import { connect } from "react-redux";
import NotificationSystem from "react-notification-system";
import PropTypes from "prop-types";
import deepEqual from "deep-equal";

import Message from "components/Message";

import {
  CLOUD_CFT_NOTIFICATION_UID,
  CLOUD_CFT_NOTIFICATION_ERROR_UID,
  CLOUD_CFT_NOTIFICATION_SUCCESS_UID,
} from "@inject/components/AddCloudModal/constants";
import {
  PROJECT_CFT_NOTIFICATION_UID,
  PROJECT_CFT_NOTIFICATION_ERROR_UID,
  PROJECT_CFT_NOTIFICATION_SUCCESS_UID,
} from "@inject/components/AddProjectModal/constants";

export class NotificationContainer extends Component {
  static propTypes = {
    notification: PropTypes.object,
  };

  constructor(props) {
    super(props);
    this.addedNotifications = [];
    this.notificationSystemRef = createRef();
  }

  componentDidMount() {
    this.notificationSystem = this.notificationSystemRef.current;
  }

  UNSAFE_componentWillReceiveProps(newProps) {
    const {
      uid,
      level,
      message,
      autoDismiss,
      detailsStyle,
      removeMessageType,
      options: { messageAction } = {},
    } = newProps.notification;
    if (removeMessageType) {
      this.removeMessages(removeMessageType, uid);
    } else if (
      // See DX-61151
      [
        CLOUD_CFT_NOTIFICATION_ERROR_UID,
        CLOUD_CFT_NOTIFICATION_SUCCESS_UID,
      ].includes(uid)
    ) {
      this.removeMessages("info", CLOUD_CFT_NOTIFICATION_UID);
    } else if (
      [
        PROJECT_CFT_NOTIFICATION_ERROR_UID,
        PROJECT_CFT_NOTIFICATION_SUCCESS_UID,
      ].includes(uid)
    ) {
      this.removeMessages("info", PROJECT_CFT_NOTIFICATION_UID);
    }

    const handleDismiss = () => {
      this.notificationSystem.removeNotification(notification);
      return false;
    };
    // suddenly got errors due to lack of actual notification being sent
    // even though not seeing a notification action happening
    // so protecting against for now
    const notification =
      message &&
      this.notificationSystem.addNotification({
        children: (
          <Message
            message={message}
            messageType={level}
            onDismiss={handleDismiss}
            detailsStyle={detailsStyle}
            messageAction={messageAction}
          />
        ),
        // message,
        dismissible: false,
        level,
        position: "tc",
        // see DX-5316 for commentary
        autoDismiss: autoDismiss || (level === "success" ? 5 : 0),
        uid: uid,
      });
    if (notification) {
      // if the notification is the same as last then remove the previous one instead of stack.
      if (deepEqual(newProps.notification, this.props.notification)) {
        this.notificationSystem.removeNotification(notification.uid - 1);
      }
      // message is defined if notification is truthy; if message has type, store it in the local list
      const messageType =
        message.messageType || (message.get && message.get("messageType"));
      if (messageType || uid) {
        this.addedNotifications.push({ messageType, notification, uid });
      }
    }
  }

  removeMessages = (messageType, uid) => {
    if (uid) {
      this.notificationSystem.removeNotification(uid);
      this.addedNotifications = this.addedNotifications.filter(
        (notification) => notification.uid !== uid,
      );
    } else if (messageType) {
      // remove messages of the given type from notification system and hence from the screen
      this.addedNotifications.forEach((entry) => {
        if (entry.messageType === messageType) {
          this.notificationSystem.removeNotification(entry.notification);
        }
      });
      // remove messages of the given type from local array
      this.addedNotifications = this.addedNotifications.filter(
        (entry) => entry.messageType !== messageType,
      );
    }
  };

  render() {
    return (
      <NotificationSystem
        ref={this.notificationSystemRef}
        style={style}
        newOnTop
      />
    );
  }
}

function mapStateToProps(state) {
  return {
    notification: state.notification,
  };
}

export default connect(mapStateToProps)(NotificationContainer);

const style = {
  Dismiss: {
    DefaultStyle: {
      width: 24,
      height: 24,
      color: "inherit",
      fontWeight: "inherit",
      backgroundColor: "none",
      top: 10,
      right: 5,
    },
  },
  NotificationItem: {
    DefaultStyle: {
      borderBottom: "none",
      boxShadow: "none",
      margin: 0,
      marginBottom: "8px",
      borderRadius: 0,
      border: "none",
      padding: 0,
    },
    success: {
      backgroundColor: "var(--fill--success)",
      borderTop: "none",
    },
    error: {
      backgroundColor: "var(--fill--danger)",
      borderTop: "none",
    },
    warning: {
      backgroundColor: "var(--fill--warning)",
      borderTop: "none",
    },
    info: {
      backgroundColor: "var(--fill--info)",
      borderTop: "none",
    },
  },
  Containers: {
    DefaultStyle: {
      padding: "0",
      width: 800,
    },
    tl: {
      left: "64px",
    },
  },
};
