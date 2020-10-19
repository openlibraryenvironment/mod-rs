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

select pr_id, 
       pr_req_inst_symbol, 
       pr_is_requester, 
       pr_hrid,
       pr_peer_request_identifier,
       pr_pick_location_fk, 
       st_code,
       pr_patron_identifier
from patron_request, 
     status 
where pr_state_fk=st_id;



select sym_id, na_symbol, sym_symbol
from symbol, naming_authority
where sym_authority_fk=na_id

update app_setting set st_value = 'admin' where st_key = 'shared_index_pass';
update app_setting set st_value = 'https://na02-psb.alma.exlibrisgroup.com/view/NCIPServlet' where st_key = 'ncip_server_address';
update app_setting set st_value = 'ki-' where st_key = 'request_id_prefix';
update app_setting set st_value = 'http://temple-psb.alma.exlibrisgroup.com:1921/01TULI_INST' where st_key = 'z3950_server_address';
update app_setting set st_value = 'TESTINST01,TESTINST02' where st_key = 'last_resort_lenders';
update app_setting set st_value = 'http://shared-index.reshare-dev.indexdata.com:9130' where st_key = 'shared_index_base_url';
update app_setting set st_value = 'diku_admin' where st_key = 'shared_index_user';

select * from refdata_value where rdv_value = 'ncip';

update app_setting set st_value = '...' where st_key = 'borrower_check';
select st_key, st_value from app_setting ;

