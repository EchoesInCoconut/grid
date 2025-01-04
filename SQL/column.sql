CREATE TABLE `column` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_id` int(11) NOT NULL,
  `sort` decimal(25,15) DEFAULT NULL,
  `header` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `cell_sort` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `table_column_idx` (`table_id`),
  CONSTRAINT `table_column` FOREIGN KEY (`table_id`) REFERENCES `table` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=116468 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
