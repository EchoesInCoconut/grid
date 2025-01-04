CREATE TABLE `row` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_id` int(11) NOT NULL,
  `sort` decimal(25,15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `table_row_idx` (`table_id`),
  CONSTRAINT `table_row` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=116498 DEFAULT CHARSET=utf8;
