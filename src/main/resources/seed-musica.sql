-- Ejecutar una sola vez sobre una base vacía (artistas, albumes y canciones).
-- Genera 500 artistas, 1,000 álbumes y 3,500 canciones: 5,000 filas en total.

INSERT INTO artistas (id, nombre, pais, fecha_formacion)
SELECT x, CONCAT('Artista de prueba ', x),
       CASE MOD(x, 5)
           WHEN 0 THEN 'México'
           WHEN 1 THEN 'Argentina'
           WHEN 2 THEN 'Colombia'
           WHEN 3 THEN 'España'
           ELSE 'Chile'
       END,
       DATEADD('YEAR', -MOD(x, 50), DATE '2025-01-01')
FROM SYSTEM_RANGE(1, 500);

INSERT INTO albumes (id, titulo, fecha_lanzamiento, genero, artista_id)
SELECT x, CONCAT('Álbum de prueba ', x),
       DATEADD('DAY', -MOD(x * 13, 9000), DATE '2025-01-01'),
       CASE MOD(x, 5)
           WHEN 0 THEN 'Rock'
           WHEN 1 THEN 'Pop'
           WHEN 2 THEN 'Jazz'
           WHEN 3 THEN 'Regional'
           ELSE 'Electrónica'
       END,
       CAST(FLOOR((x - 1) / 2) + 1 AS BIGINT)
FROM SYSTEM_RANGE(1, 1000);

INSERT INTO canciones (id, titulo, numero_pista, duracion_segundos, album_id)
SELECT x, CONCAT('Canción de prueba ', x),
       CASE WHEN x <= 2000 THEN MOD(x - 1, 4) + 1 ELSE MOD(x - 2001, 3) + 1 END,
       120 + MOD(x * 17, 240),
       CAST(CASE WHEN x <= 2000
           THEN FLOOR((x - 1) / 4) + 1
           ELSE 501 + FLOOR((x - 2001) / 3)
       END AS BIGINT)
FROM SYSTEM_RANGE(1, 3500);

ALTER TABLE artistas ALTER COLUMN id RESTART WITH 501;
ALTER TABLE albumes ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE canciones ALTER COLUMN id RESTART WITH 3501;
