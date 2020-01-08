CREATE TABLE `company` (id INT(11) PRIMARY KEY NOT NULL, `display_name` VARCHAR(255) NOT NULL, `owner_id` int(11) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `company` (`id`, `display_name`) VALUES (1, "Crazy Carrots");
INSERT INTO `company` (`id`, `display_name`) VALUES (2, "Dancing Daffodils");
INSERT INTO `company` (`id`, `display_name`) VALUES (3, "Flying Flatfish");
INSERT INTO `company` (`id`, `display_name`) VALUES (4, "Leaping Leopards");

CREATE TABLE `user` (id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, `company_id` int(11) NOT NULL, `display_name` VARCHAR(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC John");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Wilber");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Authur");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Katy");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Perry");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Britney");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (1, "CC Spears");

INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD James");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD William");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD Alan");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD Kerry");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD Prince");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD Barbara");
INSERT INTO `user` (`company_id`, `display_name`) VALUES (2, "DD Smith");

CREATE TABLE `user_private_message` (`id` INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, `timestamp` DATETIME(6) NOT NULL, `sender_id` INT(11) NOT NULL, `recipient_id` INT(11) NOT NULL, `priority` INT(11) NOT NULL, `subject` VARCHAR(255) DEFAULT NULL, `message` VARCHAR(255) NOT NULL, `unread` TINYINT(1) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `user_private_message` (`timestamp`, `sender_id`, `recipient_id`, `priority`, `subject`, `message`, `unread`) SELECT NOW(), s.id, r.id, 1, NULL, CONCAT(s.display_name, " to ", r.display_name), 1 FROM user s, user r;

