create or replace trigger t_biu_lang_resource
  before insert or update on t_lang_resource  
  for each row  
declare
  -- local variables here
begin
  if (:new.LR_ACCESS_MODE = security.PERM_WR_GW or 
      :new.LR_ACCESS_MODE = security.PERM_GR_GW or
      :new.LR_ACCESS_MODE = security.PERM_GR_OW) then

     -- group write/read access, owner_group_id should ne NOT NULL
     if (:new.LR_OWNER_GROUP_ID is null) then
        raise error.x_incomplete_data;
     end if;
  end if;
  
  if (:new.LR_ACCESS_MODE = security.PERM_GR_OW or
      :new.LR_ACCESS_MODE = security.PERM_OR_OW) then
     
     -- owner_user_id is mandatory
     if (:new.LR_OWNER_USER_ID is null) then
        raise error.x_incomplete_data;
     end if;
      
  end if;
end t_bi_lang_resource;
/
