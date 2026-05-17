create table if not exists image_uploads (
    id bigserial primary key,
    uploader_user_id bigint not null,
    category varchar(50) not null,
    status varchar(20) not null,
    temp_key varchar(1000) not null,
    final_key varchar(1000),
    size_bytes bigint not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    attached_at timestamp(6) with time zone,
    constraint chk_image_uploads_category
        check (category in ('PROFILE_IMAGE', 'CREW_COVER_IMAGE', 'MEETING_LOG_PHOTO')),
    constraint chk_image_uploads_status
        check (status in ('TEMP', 'ATTACHED', 'EXPIRED'))
);

create index if not exists idx_image_uploads_uploader_status
    on image_uploads (uploader_user_id, status);

create index if not exists idx_image_uploads_status_expires_at
    on image_uploads (status, expires_at);
