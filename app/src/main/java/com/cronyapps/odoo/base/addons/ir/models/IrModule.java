package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppConfig;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldSelection;

import java.util.Arrays;

@DataModelSetup(ModelSetup.CONFIGURATION)
@DataModel("ir.module.module")
public class IrModule extends BaseDataModel<IrModule> {

    FieldChar name = new FieldChar("Technical Name").size(100);
    FieldChar shortdesc = new FieldChar("Module").size(100);
    FieldSelection state = new FieldSelection("Status")
            .addSelection("uninstallable", "Not Installable")
            .addSelection("uninstalled", "Not Installed")
            .addSelection("installed", "Installed")
            .addSelection("to upgrade", "To be upgraded")
            .addSelection("to remove", "To be removed")
            .addSelection("to install", "To be installed");

    FieldManyToOne category_id = new FieldManyToOne("Category", IrModuleCategory.class);

    public IrModule(Context context, OdooUser user) {
        super(context, user);
    }


    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("name", "in", Arrays.asList(AppConfig.DEPENDS_ON_MODULES));
        return domain;
    }
}
