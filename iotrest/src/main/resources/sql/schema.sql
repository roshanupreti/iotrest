CREATE TABLE IF NOT EXISTS `USERS` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `FIRST_NAME` varchar(50) NOT NULL,
  `LAST_NAME` varchar(50) NOT NULL,
  `EMAIL` varchar(50) NOT NULL,
  `USERNAME` varchar(30) NOT NULL,
  `PASSWORD` text NOT NULL,
  `IS_ACTIVE` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`ID`,`EMAIL`,`USERNAME`),
  UNIQUE KEY `EMAIL` (`EMAIL`),
  UNIQUE KEY `USERNAME` (`USERNAME`)
  )
  ;

CREATE TABLE IF NOT EXISTS `USERS_ACCESS_RIGHTS` (
  `USER_ID` int(11) NOT NULL,
  `CREATE_ACCESS` tinyint(1) DEFAULT NULL,
  `READ_ACCESS` tinyint(1) DEFAULT NULL,
  `UPDATE_ACCESS` tinyint(1) DEFAULT NULL,
  `DELETE_ACCESS` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`),
  UNIQUE KEY `USER_ID` (`USER_ID`),
  FOREIGN KEY (`USER_ID`) REFERENCES `USERS` (`ID`) ON DELETE CASCADE
  )
  ;