do
$$
    begin
        if not exists(select from pg_type where typname = 'payment_mode') then
            create type payment_mode as enum (
                'BANK_TRANSFER',
                'MOBILE_BANKING',
                'CASH');
        end if;
    end
$$;

create table if not exists "member_payment"
(
    id                   varchar primary key,
    amount               numeric(12, 2),
    creation_date        date,
    member_debited_id    varchar references member ("id"),
    membership_fee_id    varchar references membership_fee ("id"),
    payment_mode         payment_mode,
    financial_account_id varchar
);