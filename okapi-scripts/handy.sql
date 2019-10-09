psql -U folio_admin -h localhost okapi_modules

set search_path to diku_mod_rs, public;

select * from patron_request ;
select pr_id, pr_date_created, pr_stitle, pr_is_requester, pr_pending_action_fk, pr_awaiting_protocol_response from patron_request;

select pr_id, pr_pick_location_fk, pr_is_requester, pr_req_inst_symbol, pr_date_created from patron_request order by pr_date_created;



select pr_id, 
       pr_pick_location_fk, 
       pr_resolved_req_inst_symbol_fk,
       pr_resolved_sup_inst_symbol_fk,
       pr_is_requester, 
       pr_req_inst_symbol, 
       pr_date_created 
from patron_request 
order by pr_date_created;


select sym_id, na_symbol, sym_symbol
from symbol, naming_authority
where sym_authority_fk=na_id
