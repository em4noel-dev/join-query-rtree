package org.obinject.sbbd2013.geonet;

import org.obinject.block.PullPage;
import org.obinject.block.PushPage;
import org.obinject.meta.Rectangle;
import org.obinject.meta.Uuid;
import org.obinject.block.Page;

public class RectLatLongCoordGeonet extends EntityCoordGeonet 
	implements Rectangle<RectLatLongCoordGeonet>
{
	private double preservedDistance;
	
    public RectLatLongCoordGeonet()
    {
    }

    public RectLatLongCoordGeonet(CoordGeonet coord)
    {
        super(coord);
    }

    public RectLatLongCoordGeonet(Uuid uuid)
    {
        super(uuid);
    }
    
    @Override
    public double distanceTo(RectLatLongCoordGeonet metric)
    {
        return Math.sqrt(
                Math.pow(this.getLatitude() - metric.getLatitude(), 2)
                + Math.pow(this.getLongitude() - metric.getLongitude(), 2));
    }

    @Override
    public double getOrigin(int idx)
    {
        if (idx == 0)
        {
            return this.getLatitude();
        } else
        {
            return this.getLongitude();
        }
    }

    @Override
    public int numberOfDimensions()
    {
        return 2;
    }

    @Override
    public boolean pullKey(byte[] array, int position)
    {
        PullPage pull = new PullPage(array, position);
        this.setLatitude(pull.pullDouble());
        this.setLongitude(pull.pullDouble());
        this.setWidth(pull.pullDouble());
        this.setHeight(pull.pullDouble());
        return true;
    }

    @Override
    public void pushKey(byte[] array, int position)
    {
        PushPage push = new PushPage(array, position);
        push.pushDouble(this.getLatitude());
        push.pushDouble(this.getLongitude());
        push.pushDouble(this.getWidth());
        push.pushDouble(this.getHeight());
    }

    @Override
    public void setOrigin(int idx, double value)
    {
        if (idx == 0)
        {
            this.setLatitude(value);
        } else
        {
            this.setLongitude(value);
        }
    }

    @Override
    public int sizeOfKey()
    {
        return Page.sizeOfDouble * 4;
    }

    @Override
    public double getExtension(int axis)
    {
        if (axis == 0)
        {
            return this.getWidth();
        } else
        {
            return this.getHeight();
        }
    }

    @Override
    public void setExtension(int axis, double value)
    {
        if (axis == 0)
        {
            this.setWidth(value);
        } else
        {
            this.setHeight(value);
        }
    }

    @Override
    public boolean hasSameKey(RectLatLongCoordGeonet key) {
        return this.getLatitude()==key.getLatitude() &&
        		this.getLongitude()==key.getLongitude() &&
        		this.getWidth()==key.getWidth() &&
        		this.getHeight()==key.getHeight();
    }

	@Override
	public double getPreservedDistance() {
		return preservedDistance;
	}

	@Override
	public void setPreservedDistance(double distance) {
		preservedDistance=distance;
	}

}
