BEGIN TRANSACTION;
CREATE TABLE `resume_send_table` (
	`current_sms`	INTEGER,
	`md5hash`	TEXT
);
);
CREATE TABLE `plugin_sent_table` (`plugin_name`	TEXT, `sent_time`	INTEGER);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925186158);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925579160);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925692096);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925807936);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925807936);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925807936);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender_plugin_2',1431925807936);
INSERT INTO `plugin_sent_table` VALUES ('ru.dsoft38.sms_sender',1431925807936);
CREATE TABLE android_metadata (locale TEXT);
INSERT INTO `android_metadata` VALUES ('ru_RU');
COMMIT;
