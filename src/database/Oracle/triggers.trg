create or replace trigger t_biu_lang_resource
  before insert or update on t_lang_resource  
  for each row  
declare
  -- local variables here
begin
  
  if (false = security.is_valid_security_data(:new.LR_ACCESS_MODE,
                                              :new.LR_OWNER_GROUP_ID,
                                              :new.LR_OWNER_USER_ID)) then

     raise error.x_incomplete_data;
     
  end if;
end t_bi_lang_resource;
/
