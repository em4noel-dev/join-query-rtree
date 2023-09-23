package org.obinject.sbbd2013.geonet;

import org.obinject.meta.Entity;
import org.obinject.block.PullPage;
import org.obinject.block.PushPage;
import org.obinject.block.Page;
import org.obinject.meta.Uuid;

public class EntityCoordGeonet extends CoordGeonet implements Entity<EntityCoordGeonet>
{

    public static final Uuid classId = Uuid.fromString("22CF318D-9CD7-C47C-D062-D1CD32AE732D");
    private Uuid uuid = Uuid.generator();

    public EntityCoordGeonet()
    {
    }

    public EntityCoordGeonet(CoordGeonet coord)
    {
        this.setLatitude(coord.getLatitude());
        this.setLongitude(coord.getLongitude());
    }

    public EntityCoordGeonet(Uuid uuid)
    {
        this.uuid = uuid;
    }
    
    @Override
    public boolean isEqual(EntityCoordGeonet object)
    {
        return this.getLatitude() == object.getLatitude()
                && this.getLongitude() == object.getLongitude();
    }

    @Override
    public Uuid getUuid()
    {
        return uuid;
    }

    @Override
    public boolean pullEntity(byte[] array, int position)
    {
        PullPage pull = new PullPage(array, position);
        Uuid storedClass = pull.pullUuid();
        if (classId.equals(storedClass) == true)
        {
            uuid = pull.pullUuid();
            this.setLatitude(pull.pullDouble());
            this.setLongitude(pull.pullDouble());
            return true;
        }
        return false;
    }

    @Override
    public void pushEntity(byte[] array, int position)
    {
        PushPage push = new PushPage(array, position);
        push.pushUuid(classId);
        push.pushUuid(uuid);
        push.pushDouble(this.getLatitude());
        push.pushDouble(this.getLongitude());
    }

    @Override
    public int sizeOfEntity()
    {
        return Page.sizeOfUuid
                + Page.sizeOfUuid
                + Page.sizeOfDouble
                + Page.sizeOfDouble;
    }    
    
	public static Uuid getClassId() {
		return classId;
	}
    
}
