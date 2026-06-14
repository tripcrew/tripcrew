ALTER TABLE attractions
    ADD FULLTEXT INDEX ft_attractions_keyword (title, addr1, addr2);
