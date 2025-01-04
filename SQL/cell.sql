CREATE TABLE `cell` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_id` int(11) NOT NULL,
  `column_id` int(11) NOT NULL,
  `table_id` int(11) NOT NULL,
  `value` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `row_column_idx` (`row_id`,`column_id`),
  KEY `row_cell_idx` (`row_id`),
  KEY `column_cell_idx` (`column_id`),
  KEY `table_cell_idx` (`table_id`),
  CONSTRAINT `column_cell` FOREIGN KEY (`column_id`) REFERENCES `column` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `row_cell` FOREIGN KEY (`row_id`) REFERENCES `row` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=777 DEFAULT CHARSET=utf8;
