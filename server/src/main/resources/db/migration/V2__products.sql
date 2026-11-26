create table products.product (
    barcode text primary key,
    name text,
    brand text,
    image_url text,
    serving_quantity text,
    category text not null,
    label_language text,
    ingredients_text text,
    ingredients jsonb,
    nutri_score text,
    nova_group smallint,
    energy_kcal_per_100g double precision,
    energy_kcal_per_serving double precision,
    fat_per_100g double precision,
    fat_per_serving double precision,
    saturated_fat_per_100g double precision,
    saturated_fat_per_serving double precision,
    carbohydrates_per_100g double precision,
    carbohydrates_per_serving double precision,
    sugars_per_100g double precision,
    sugars_per_serving double precision,
    fibre_per_100g double precision,
    fibre_per_serving double precision,
    proteins_per_100g double precision,
    proteins_per_serving double precision,
    salt_per_100g double precision,
    salt_per_serving double precision,
    fetched_at timestamptz not null,
    created_at timestamptz not null default now()
);

create table products.product_missing (
    barcode text primary key,
    checked_at timestamptz not null,
    created_at timestamptz not null default now()
);
