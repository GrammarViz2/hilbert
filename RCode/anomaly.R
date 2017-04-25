require(ggplot2)
require(Cairo)
require(reshape)
require(grid)
require(gridExtra)
require(scales)

dat=read.csv("../data/period/anomaly_hilbert_curve.csv",header=F)
names(dat) = c("index","value","points")
par(mfrow=c(2,1))
plot(dat$value[1:(length(dat$value)-10)], col="blue", type="l", main=
       "Hilbert cuve of the test trajectory")
plot(dat$points[1:(length(dat$points)-10)], col="blue", type="l", main=
       "Count of reference points in cells")

dat=as.matrix(read.csv("dat.txt",header=F))


normal = data.frame(x=c(1:length(dat[1,])), normal=dat[1,], normal_color=rescale(dat[2,]))
anomaly = data.frame(x=c(1:length(dat[3,])), anomaly=dat[3,], anomaly_color=rescale(dat[4,]))


normal_p = ggplot(normal, aes(x=x,y=normal,color=normal_color)) + geom_line(size=2) + 
  scale_color_gradient("Class Specificity", low="green",high="red", space = "Lab") + 
  ggtitle("Normal Path")
normal_p

anomaly_p = ggplot(anomaly, aes(x=x,y=anomaly,color=anomaly_color)) + geom_line(size=2) + 
  scale_color_gradient("Class Specificity", low="green",high="red", space = "Lab")+ 
  ggtitle("Path with Anomaly")
anomaly_p

grid.arrange(normal_p,anomaly_p,ncol=1)
