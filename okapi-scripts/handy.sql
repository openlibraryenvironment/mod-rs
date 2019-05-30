psql -U folio_admin -h localhost okapi_modules

set search_path to diku_mod_rs, public;

select * from patron_request ;
select pr_id, pr_date_created, pr_stitle, pr_is_requester, pr_pending_action_fk, pr_awaiting_protocol_response from patron_request;

